"""
Legal Events Hub — simple file-based store.
Persists events, registrations, and questions to JSON files.
Both the student portal (read/write) and admin/principal portal (write) hit this endpoint.
"""
from typing import Any, List
from fastapi import APIRouter, Body, Depends
import os
import json

from app.core.dependencies import get_current_user, role_required
from app.db.models.user import User, UserRole

from app.core.json_db_helper import load_json_store, save_json_store

router = APIRouter()

STAFF_ROLES = [UserRole.FACULTY, UserRole.HOD, UserRole.PRINCIPAL, UserRole.ADMIN, UserRole.SUPER_ADMIN]
APPROVAL_ROLES = [UserRole.PRINCIPAL, UserRole.ADMIN, UserRole.SUPER_ADMIN]

# ─── File Paths ──────────────────────────────────────────────────────────────
DB_DIR = os.path.dirname(os.path.dirname(os.path.dirname(os.path.dirname(__file__))))
EVENTS_FILE = os.path.join(DB_DIR, "legal_events_db.json")
REGISTRATIONS_FILE = os.path.join(DB_DIR, "legal_registrations_db.json")
QUESTIONS_FILE = os.path.join(DB_DIR, "legal_questions_db.json")

# ─── Default Data ─────────────────────────────────────────────────────────────
DEFAULT_EVENTS = [
    {
        "id": "LEH-2026-001",
        "title": "Constitutional Law in the Digital Age",
        "category": "Constitutional Law",
        "speaker": {
            "name": "Hon. D.Y. Chandrachud",
            "designation": "Former Chief Justice of India",
            "type": "Supreme Court Judge",
            "bio": "50th Chief Justice of India. Known for landmark judgments on Right to Privacy, Section 377, and Sabarimala. Champion of digital modernization of Indian judiciary.",
            "initials": "DC"
        },
        "date": "20 Jun 2026",
        "time": "10:00 AM",
        "duration": "120 min",
        "status": "Registration Open",
        "mode": "Online",
        "platform": "Zoom Webinar",
        "meetingLink": "https://zoom.us/j/leh2026001",
        "totalSeats": 500,
        "availableSeats": 127,
        "registrationDeadline": "18 Jun 2026",
        "description": "An in-depth exploration of how constitutional principles are being challenged and reshaped by emerging digital technologies. Justice Chandrachud will discuss the intersection of fundamental rights, privacy, surveillance, and the evolving digital landscape in Indian constitutional jurisprudence.",
        "agenda": [
            "Fundamental Rights in the Digital Era",
            "Right to Privacy — Puttaswamy Judgment & Its Impact",
            "Data Protection & Constitutional Safeguards",
            "AI and the Rule of Law",
            "Interactive Q&A with Justice Chandrachud"
        ],
        "activityPoints": 5,
        "materialsAvailable": False,
        "certificateAvailable": True,
        "eventType": "Intra-College",
        "organizingInstitute": "CAMS Law College"
    },
    {
        "id": "LEH-2026-002",
        "title": "Cyber Crime Investigation & Digital Evidence",
        "category": "Cyber Law",
        "speaker": {
            "name": "Hon. Uday Umesh Lalit",
            "designation": "Former Chief Justice of India",
            "type": "Supreme Court Judge",
            "bio": "49th Chief Justice of India. Renowned for judicial reforms, live-streaming of court proceedings, and expertise in constitutional and cyber law matters.",
            "initials": "UL"
        },
        "date": "22 Jun 2026",
        "time": "02:00 PM",
        "duration": "90 min",
        "status": "Registration Open",
        "mode": "Online",
        "platform": "Google Meet",
        "meetingLink": "https://meet.google.com/leh-2026-002",
        "totalSeats": 300,
        "availableSeats": 85,
        "registrationDeadline": "20 Jun 2026",
        "description": "A comprehensive session on the legal framework for cyber crime investigation, admissibility of digital evidence under the Indian Evidence Act, and the challenges posed by emerging technologies in criminal prosecution.",
        "agenda": [
            "Cyber Crime Landscape in India",
            "IT Act 2000 — Key Provisions & Amendments",
            "Digital Evidence: Collection, Preservation & Admissibility",
            "Section 65B Certificate — Judicial Interpretation",
            "Case Studies: Landmark Cyber Crime Cases"
        ],
        "activityPoints": 4,
        "materialsAvailable": False,
        "certificateAvailable": True,
        "eventType": "Intra-College",
        "organizingInstitute": "CAMS Law College"
    },
    {
        "id": "LEH-2026-003",
        "title": "Career Opportunities in Judiciary",
        "category": "Career Guidance",
        "speaker": {
            "name": "Hon. Indira Banerjee",
            "designation": "Former Judge, Supreme Court of India",
            "type": "Supreme Court Judge",
            "bio": "Distinguished jurist and former Chief Justice of Madras High Court. Known for promoting women's representation in the judiciary and mentoring young legal professionals.",
            "initials": "IB"
        },
        "date": "25 Jun 2026",
        "time": "11:00 AM",
        "duration": "90 min",
        "status": "Upcoming",
        "mode": "Online",
        "platform": "Microsoft Teams",
        "meetingLink": "https://teams.microsoft.com/l/leh2026003",
        "totalSeats": 400,
        "availableSeats": 212,
        "registrationDeadline": "23 Jun 2026",
        "description": "An exclusive mentoring session covering all aspects of career paths within the Indian judiciary — from civil judge to Supreme Court, including judicial services exam preparation, eligibility criteria, interview techniques, and career progression.",
        "agenda": [
            "Judicial Career Pathways in India",
            "State Judicial Services vs. Higher Judiciary",
            "Exam Preparation Strategy & Study Plan",
            "Interview & Viva Preparation Tips",
            "Q&A: Ask Justice Banerjee"
        ],
        "activityPoints": 3,
        "materialsAvailable": False,
        "certificateAvailable": True,
        "eventType": "Intra-College",
        "organizingInstitute": "CAMS Law College"
    },
    {
        "id": "LEH-2026-004",
        "title": "Corporate Governance & SEBI Regulations",
        "category": "Corporate Law",
        "speaker": {
            "name": "Adv. Harish Salve",
            "designation": "Senior Advocate, Supreme Court of India",
            "type": "Senior Advocate",
            "bio": "One of India's most eminent Senior Advocates. Former Solicitor General of India. Known for representing India in the ICJ (Kulbhushan Jadhav case) and expertise in corporate and constitutional law.",
            "initials": "HS"
        },
        "date": "28 Jun 2026",
        "time": "03:00 PM",
        "duration": "120 min",
        "status": "Registration Open",
        "mode": "Online",
        "platform": "Zoom Webinar",
        "meetingLink": "https://zoom.us/j/leh2026004",
        "totalSeats": 350,
        "availableSeats": 198,
        "registrationDeadline": "26 Jun 2526",
        "description": "A masterclass on the evolving landscape of corporate governance in India, covering SEBI regulations, board accountability, shareholder rights, ESG compliance, and the role of legal counsel in corporate decision-making.",
        "agenda": [
            "SEBI LODR Regulations — Key Compliance Requirements",
            "Board Composition & Independent Directors",
            "Related Party Transactions & Disclosure Norms",
            "ESG & Corporate Social Responsibility",
            "Case Study: Recent SEBI Enforcement Actions"
        ],
        "activityPoints": 5,
        "materialsAvailable": False,
        "certificateAvailable": True,
        "eventType": "Intra-College",
        "organizingInstitute": "CAMS Law College"
    },
    {
        "id": "LEH-2026-005",
        "title": "Human Rights Advocacy in Modern India",
        "category": "Human Rights",
        "speaker": {
            "name": "Dr. Vrinda Grover",
            "designation": "Senior Advocate & Human Rights Lawyer",
            "type": "Human Rights Activist",
            "bio": "Award-winning human rights lawyer. Known for representing victims of communal violence, custodial torture, and gender-based violence. Recipient of the Franco-German Human Rights Prize.",
            "initials": "VG"
        },
        "date": "01 Jul 2026",
        "time": "10:30 AM",
        "duration": "90 min",
        "status": "Upcoming",
        "mode": "Online",
        "platform": "Google Meet",
        "meetingLink": "https://meet.google.com/leh-2026-005",
        "totalSeats": 250,
        "availableSeats": 250,
        "registrationDeadline": "29 Jun 2026",
        "description": "A powerful session exploring contemporary human rights challenges in India — from constitutional protections to international human rights law, documenting violations, strategic litigation, and building a career in human rights advocacy.",
        "agenda": [
            "Constitutional Framework of Human Rights in India",
            "NHRC & State Human Rights Commissions",
            "International Human Rights Mechanisms",
            "Strategic Litigation for Social Justice",
            "Building a Career in Human Rights Law"
        ],
        "activityPoints": 4,
        "materialsAvailable": False,
        "certificateAvailable": True,
        "eventType": "Intra-College",
        "organizingInstitute": "CAMS Law College"
    },
    {
        "id": "LEH-2026-006",
        "title": "Intellectual Property in the AI Era",
        "category": "Intellectual Property Rights",
        "speaker": {
            "name": "Prof. Shamnad Basheer",
            "designation": "IP Law Scholar & Author",
            "type": "Legal Scholar",
            "bio": "Renowned intellectual property law scholar and founder of SpicyIP, India's leading IP law blog. Expert on patent law, access to medicines, and the intersection of AI and intellectual property.",
            "initials": "SB"
        },
        "date": "04 Jul 2026",
        "time": "02:00 PM",
        "duration": "120 min",
        "status": "Upcoming",
        "mode": "Online",
        "platform": "Zoom Webinar",
        "meetingLink": "https://zoom.us/j/leh2026006",
        "totalSeats": 200,
        "availableSeats": 200,
        "registrationDeadline": "02 Jul 2026",
        "description": "Who owns AI-generated art? Can AI be an inventor? This cutting-edge session dives deep into the intellectual property challenges posed by artificial intelligence, covering patent eligibility, copyright ownership, and the future of IP law.",
        "agenda": [
            "AI & Inventorship: The DABUS Patent Controversy",
            "Copyright in AI-Generated Works",
            "Trade Secrets & AI Training Data",
            "IP Strategy for AI Startups",
            "Panel Discussion: Future of IP in India"
        ],
        "activityPoints": 5,
        "materialsAvailable": False,
        "certificateAvailable": True,
        "eventType": "Inter-College",
        "organizingInstitute": "SpicyIP & NLSIU Bangalore"
    },
    {
        "id": "LEH-2026-007",
        "title": "Moot Court Mastery Workshop",
        "category": "Moot Court Workshop",
        "speaker": {
            "name": "Justice B.N. Srikrishna",
            "designation": "Former Judge, Supreme Court of India",
            "type": "Supreme Court Judge",
            "bio": "Former Supreme Court Judge and Chairman of the Data Protection Committee. Known for his sharp legal acumen and mentoring moot court competitors. Author of the Srikrishna Committee Report on Data Protection.",
            "initials": "BS"
        },
        "date": "07 Jul 2026",
        "time": "09:00 AM",
        "duration": "180 min",
        "status": "Upcoming",
        "mode": "Hybrid",
        "platform": "Zoom + Campus Court Room",
        "meetingLink": "https://zoom.us/j/leh2026007",
        "totalSeats": 150,
        "availableSeats": 150,
        "registrationDeadline": "05 Jul 2026",
        "description": "An intensive hands-on workshop for aspiring moot court competitors. Justice Srikrishna will share insights on memorial drafting, oral advocacy techniques, bench etiquette, and winning strategies for national and international moot court competitions.",
        "agenda": [
            "Understanding the Moot Court Proposition",
            "Memorial Drafting: Structure & Persuasion",
            "Oral Advocacy: Delivery, Timing & Rebuttal",
            "Bench Etiquette & Handling Difficult Questions",
            "Mock Round with Live Feedback from Justice Srikrishna"
        ],
        "activityPoints": 8,
        "materialsAvailable": False,
        "certificateAvailable": True,
        "eventType": "Intra-College",
        "organizingInstitute": "CAMS Law College"
    },
    {
        "id": "LEH-2026-008",
        "title": "Legal Research Methodology & Academic Writing",
        "category": "Legal Research",
        "speaker": {
            "name": "Prof. M.P. Singh",
            "designation": "Vice-Chancellor, NLSIU Bangalore (Emeritus)",
            "type": "Law Professor",
            "bio": "Distinguished legal academician and former Vice-Chancellor of National Law School. Author of over 20 books on constitutional law and legal research methodology. Pioneer in legal education reforms.",
            "initials": "MS"
        },
        "date": "10 Jul 2026",
        "time": "11:00 AM",
        "duration": "120 min",
        "status": "Upcoming",
        "mode": "Online",
        "platform": "Google Meet",
        "meetingLink": "https://meet.google.com/leh-2026-008",
        "totalSeats": 300,
        "availableSeats": 300,
        "registrationDeadline": "08 Jul 2026",
        "description": "A comprehensive workshop on advanced legal research techniques, covering primary and secondary sources, database navigation (Manupatra, SCC Online, Westlaw), citation standards (Bluebook, OSCOLA), and strategies for publishing in law reviews.",
        "agenda": [
            "Primary vs. Secondary Legal Research Sources",
            "Mastering Legal Databases: Manupatra & SCC Online",
            "Citation Standards: Bluebook & OSCOLA Formats",
            "Writing Case Comments & Law Review Articles",
            "Publication Strategy for Law Students"
        ],
        "activityPoints": 4,
        "materialsAvailable": False,
        "certificateAvailable": True,
        "eventType": "Intra-College",
        "organizingInstitute": "CAMS Law College"
    },
    {
        "id": "LEH-2026-009",
        "title": "Criminal Law Reform: Contemporary Issues",
        "category": "Criminal Law",
        "speaker": {
            "name": "Justice Madan B. Lokur",
            "designation": "Former Judge, Supreme Court of India",
            "type": "Supreme Court Judge",
            "bio": "Former Supreme Court Judge and current Judge at the Supreme Court of Fiji. Champion of judicial reforms, environmental justice, and access to justice for marginalized communities.",
            "initials": "ML"
        },
        "date": "14 Jul 2026",
        "time": "10:00 AM",
        "duration": "90 min",
        "status": "Upcoming",
        "mode": "Online",
        "platform": "Microsoft Teams",
        "meetingLink": "https://teams.microsoft.com/l/leh2026009",
        "totalSeats": 400,
        "availableSeats": 400,
        "registrationDeadline": "12 Jul 2026",
        "description": "An insightful session on the ongoing transformation of criminal law in India — covering the Bharatiya Nyaya Sanhita (BNS), Bharatiya Nagarik Suraksha Sanhita (BNSS), and their impact on criminal prosecution, victims' rights, and bail reform.",
        "agenda": [
            "BNS 2023: Key Changes from IPC",
            "BNSS 2023: Procedural Reforms",
            "Bail Reform & Undertrial Justice",
            "Victim-Centric Criminal Justice",
            "Interactive Discussion with Justice Lokur"
        ],
        "activityPoints": 4,
        "materialsAvailable": False,
        "certificateAvailable": True,
        "eventType": "Inter-College",
        "organizingInstitute": "National Law University, Delhi"
    },
    {
        "id": "LEH-2026-010",
        "title": "Judicial Services Preparation Masterclass",
        "category": "Judicial Services Prep",
        "speaker": {
            "name": "Hon. Ranjan Gogoi",
            "designation": "Former Chief Justice of India",
            "type": "Supreme Court Judge",
            "bio": "46th Chief Justice of India. Known for landmark judgments including the Assam NRC verdict and the Ayodhya case. Strong advocate for judicial reforms and efficient court administration.",
            "initials": "RG"
        },
        "date": "18 Jul 2026",
        "time": "10:00 AM",
        "duration": "150 min",
        "status": "Upcoming",
        "mode": "Online",
        "platform": "Zoom Webinar",
        "meetingLink": "https://zoom.us/j/leh2026010",
        "totalSeats": 500,
        "availableSeats": 500,
        "registrationDeadline": "16 Jul 2026",
        "description": "A comprehensive masterclass for students aspiring to join the judiciary through state judicial service examinations. Former CJI Ranjan Gogoi shares his wisdom on the qualities that make an exceptional judicial officer, preparation strategies, and the evolving expectations from the bench.",
        "agenda": [
            "Overview of Judicial Services Examination Pattern",
            "Subject-wise Preparation Strategy",
            "Answer Writing Techniques for Judiciary Exams",
            "Mock Interview & Viva Preparation",
            "Personal Insights from CJI Gogoi's Judicial Career"
        ],
        "activityPoints": 6,
        "materialsAvailable": False,
        "certificateAvailable": True,
        "eventType": "Inter-College",
        "organizingInstitute": "NALSAR University of Law, Hyderabad"
    }
]

# ─── Load & Save Helpers ──────────────────────────────────────────────────────
def load_data(filepath: str, default: Any) -> Any:
    """Read a legal-events document store from the database.

    These were plain files inside the application directory, which does not survive a
    container redeploy — registrations and submitted questions were lost on each
    deploy. The row is seeded once from the existing file, then the database is
    authoritative.
    """
    return load_json_store(filepath, lambda: default)


def save_data(filepath: str, data: Any):
    save_json_store(filepath, data)


# ─── Events CRUD ──────────────────────────────────────────────────────────────
@router.get("", summary="Get all legal events")
def get_events(skip: int = 0, limit: int = 100, current_user: User = Depends(get_current_user)) -> List[Any]:
    events = load_data(EVENTS_FILE, DEFAULT_EVENTS)
    return events[skip: skip + limit]


@router.post("", summary="Replace the full events list")
def save_events(events: List[Any] = Body(...), current_user: User = Depends(role_required(APPROVAL_ROLES))) -> dict:
    save_data(EVENTS_FILE, events)
    return {"ok": True, "count": len(events)}


# ─── Registrations ────────────────────────────────────────────────────────────
@router.get("/registrations", summary="Get event registrations, optionally scoped to one student")
def get_registrations(student_email: str | None = None, current_user: User = Depends(get_current_user)) -> List[Any]:
    regs = load_data(REGISTRATIONS_FILE, [])
    if student_email:
        regs = [r for r in regs if r.get("studentEmail") == student_email]
    return regs


@router.post("/registrations", summary="Register for an event")
def register_for_event(registration: Any = Body(...), current_user: User = Depends(get_current_user)) -> dict:
    regs = load_data(REGISTRATIONS_FILE, [])
    regs.append(registration)
    save_data(REGISTRATIONS_FILE, regs)
    return {"ok": True, "count": len(regs)}


@router.put("/registrations", summary="Update registration details")
def update_registration(payload: Any = Body(...), current_user: User = Depends(get_current_user)) -> dict:
    regs = load_data(REGISTRATIONS_FILE, [])
    updated = False
    for r in regs:
        if r.get("eventId") == payload.get("eventId") and r.get("studentEmail") == payload.get("studentEmail"):
            r["attended"] = payload.get("attended", r.get("attended", False))
            r["status"] = payload.get("status", r.get("status", "Confirmed"))
            updated = True
    if updated:
        save_data(REGISTRATIONS_FILE, regs)
    return {"ok": True, "updated": updated}


# ─── Ask a Judge — Questions ──────────────────────────────────────────────────
@router.get("/questions", summary="Get submitted questions, optionally scoped to one student")
def get_questions(student_email: str | None = None, current_user: User = Depends(get_current_user)) -> List[Any]:
    qs = load_data(QUESTIONS_FILE, [])
    if student_email:
        qs = [q for q in qs if q.get("studentEmail") == student_email]
    return qs


@router.post("/questions", summary="Submit a question for Ask a Judge")
def submit_question(question: Any = Body(...), current_user: User = Depends(get_current_user)) -> dict:
    qs = load_data(QUESTIONS_FILE, [])
    qs.append(question)
    save_data(QUESTIONS_FILE, qs)
    return {"ok": True, "count": len(qs)}

# ─── Faculty Event Posting ────────────────────────────────────────────────────
from datetime import datetime

@router.post("/faculty", summary="Post a legal event (Pending Review)")
def faculty_post_event(event: Any = Body(...), current_user: User = Depends(role_required(STAFF_ROLES))) -> dict:
    events = load_data(EVENTS_FILE, DEFAULT_EVENTS)
    if not event.get("id"):
        event["id"] = f"LEH-TEMP-{int(datetime.now().timestamp())}"
    event["status"] = "Pending"
    event["posted_by"] = current_user.id
    event["posted_by_name"] = current_user.full_name
    events.append(event)
    save_data(EVENTS_FILE, events)
    return {"ok": True, "event": event}


@router.get("/pending", summary="Get all pending events for Principal review")
def get_pending_events(current_user: User = Depends(role_required(APPROVAL_ROLES))) -> List[Any]:
    events = load_data(EVENTS_FILE, DEFAULT_EVENTS)
    return [e for e in events if e.get("status") == "Pending"]


@router.patch("/{event_id}/approve", summary="Approve and publish event")
def approve_event(event_id: str, current_user: User = Depends(role_required(APPROVAL_ROLES))) -> dict:
    events = load_data(EVENTS_FILE, DEFAULT_EVENTS)
    updated = False
    for e in events:
        if e.get("id") == event_id:
            e["status"] = "Upcoming"
            updated = True
            break
    if updated:
        save_data(EVENTS_FILE, events)
    return {"ok": True, "updated": updated}


@router.patch("/{event_id}/reject", summary="Reject event")
def reject_event(event_id: str, remarks: str = Body(None), current_user: User = Depends(role_required(APPROVAL_ROLES))) -> dict:
    events = load_data(EVENTS_FILE, DEFAULT_EVENTS)
    updated = False
    for e in events:
        if e.get("id") == event_id:
            e["status"] = "Rejected"
            if remarks:
                e["rejection_remarks"] = remarks
            updated = True
            break
    if updated:
        save_data(EVENTS_FILE, events)
    return {"ok": True, "updated": updated}
