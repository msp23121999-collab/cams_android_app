from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy.ext.asyncio import AsyncSession
from typing import List

from app.core.dependencies import get_current_user, get_db_session
from app.db.models.user import User
from app.schemas.chatbot import ChatRequest, ChatResponse, ChatSessionResponse, ChatSessionListResponse, ChatMessageResponse
from app.services.chatbot_service import ChatbotService

router = APIRouter()

@router.post("/message", response_model=ChatResponse)
async def send_message(
    request: ChatRequest,
    current_user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db_session)
):
    """Send a message to the Academic Assistant"""
    try:
        return await ChatbotService.process_message(db, current_user, request.message, request.session_id)
    except ValueError as e:
        raise HTTPException(status_code=400, detail=str(e))

@router.get("/history", response_model=List[ChatSessionListResponse])
async def get_history(
    current_user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db_session)
):
    """Get all chat sessions for the current user"""
    return await ChatbotService.get_history(db, current_user.id)

@router.get("/session/{session_id}", response_model=List[ChatMessageResponse])
async def get_session_messages(
    session_id: str,
    current_user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db_session)
):
    """Get messages for a specific session"""
    return await ChatbotService.get_session_messages(db, current_user.id, session_id)

@router.delete("/session/{session_id}", status_code=status.HTTP_204_NO_CONTENT)
async def delete_session(
    session_id: str,
    current_user: User = Depends(get_current_user),
    db: AsyncSession = Depends(get_db_session)
):
    """Delete a chat session"""
    success = await ChatbotService.clear_session(db, current_user.id, session_id)
    if not success:
        raise HTTPException(status_code=404, detail="Session not found or already deleted")
