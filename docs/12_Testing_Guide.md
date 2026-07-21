# 12. Testing Guide

## QA Strategy
- **Backend:** Comprehensive pytest suite. `test_access_control.py` validates RBAC. `test_schema_integrity.py` validates DB schemas.
- **Android:** Espresso UI tests and JUnit tests for business logic.

## Execution
```bash
cd backend
python -m pytest tests/
```
