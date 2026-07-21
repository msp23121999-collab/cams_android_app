from pydantic import BaseModel


class CreateOrderRequest(BaseModel):
    amount: float  # amount in rupees the student intends to pay (supports partial payment)


class CreateOrderResponse(BaseModel):
    order_id: str
    amount: float
    currency: str = "INR"
    key_id: str


class VerifyPaymentRequest(BaseModel):
    razorpay_order_id: str
    razorpay_payment_id: str
    razorpay_signature: str


class VerifyPaymentResponse(BaseModel):
    status: str  # "paid" | "failed"
    record_id: str
    fee_status: str | None = None
