"""
Generic API response models.
"""
from typing import TypeVar, Generic, Optional
from pydantic import BaseModel

T = TypeVar('T')


class ErrorDetail(BaseModel):
    """Error detail for failed responses."""
    code: str
    message: str


class ApiResponse(BaseModel, Generic[T]):
    """
    Generic API response wrapper.
    
    All endpoints return this structure:
    - success: True/False
    - data: Response data (if success)
    - error: Error detail (if failed)
    """
    success: bool
    data: Optional[T] = None
    error: Optional[ErrorDetail] = None
    
    class Config:
        from_attributes = True
