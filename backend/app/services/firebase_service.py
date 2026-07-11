import logging
import httpx

logger = logging.getLogger(__name__)

FIREBASE_URL = "https://cams-law-default-rtdb.firebaseio.com"

class FirebaseService:
    @staticmethod
    async def sync_faculty(
        faculty_id: str,
        faculty_name: str,
        department: str,
        status: str,
        check_in: str | None = None,
        check_out: str | None = None
    ) -> bool:
        """
        Synchronize today's attendance details for a specific faculty to Firebase.
        """
        url = f"{FIREBASE_URL}/attendance/{faculty_id}.json"
        payload = {
            "faculty_id": faculty_id,
            "faculty_name": faculty_name,
            "department": department,
            "status": status,
            "check_in": True if check_in else False,
            "check_out": True if check_out else False
        }
        try:
            async with httpx.AsyncClient(timeout=5.0) as client:
                res = await client.put(url, json=payload)
                if res.status_code in (200, 201):
                    return True
                logger.error(f"Firebase Sync failed status={res.status_code} body={res.text}")
        except Exception as e:
            logger.error(f"Firebase Sync Exception: {str(e)}")
        return False

    @staticmethod
    async def delete_faculty(faculty_id: str) -> bool:
        """
        Remove a faculty from Firebase attendance layer.
        """
        url = f"{FIREBASE_URL}/attendance/{faculty_id}.json"
        try:
            async with httpx.AsyncClient(timeout=5.0) as client:
                res = await client.delete(url)
                if res.status_code in (200, 204):
                    return True
                logger.error(f"Firebase Delete failed status={res.status_code} body={res.text}")
        except Exception as e:
            logger.error(f"Firebase Delete Exception: {str(e)}")
        return False

    @staticmethod
    async def bulk_sync(attendance_dict: dict) -> bool:
        """
        Overwrite the entire attendance node in Firebase (used for daily resets).
        attendance_dict format: { faculty_id: { faculty_id, faculty_name, department, status, check_in, check_out } }
        """
        url = f"{FIREBASE_URL}/attendance.json"
        mapped_dict = {}
        for fid, val in attendance_dict.items():
            mapped_dict[fid] = {
                "faculty_id": val.get("faculty_id"),
                "faculty_name": val.get("faculty_name"),
                "department": val.get("department"),
                "status": val.get("status"),
                "check_in": True if val.get("check_in") else False,
                "check_out": True if val.get("check_out") else False
            }
        try:
            async with httpx.AsyncClient(timeout=10.0) as client:
                res = await client.put(url, json=mapped_dict)
                if res.status_code in (200, 201):
                    return True
                logger.error(f"Firebase Bulk Sync failed status={res.status_code} body={res.text}")
        except Exception as e:
            logger.error(f"Firebase Bulk Sync Exception: {str(e)}")
        return False
