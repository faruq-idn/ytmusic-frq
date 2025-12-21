"""
Thumbnail URL utilities for YouTube Music images.
"""
import re


def transform_thumbnail_url(url: str, width: int = 800, height: int = 800) -> str:
    """
    Transform YouTube thumbnail URL to higher resolution.
    
    YouTube Music thumbnails come with low resolution by default (60x60 or 120x120).
    This function transforms them to higher resolution.
    
    Args:
        url: Original thumbnail URL
        width: Desired width (default 800)
        height: Desired height (default 800)
    
    Returns:
        Transformed URL with higher resolution
    
    Example:
        Input:  "https://lh3.googleusercontent.com/...=w60-h60-l90-rj"
        Output: "https://lh3.googleusercontent.com/...=w800-h800-l90-rj"
    """
    if not url:
        return ""
    
    # Pattern: =w60-h60 or =w120-h120 etc.
    pattern = r'=w\d+-h\d+'
    replacement = f'=w{width}-h{height}'
    
    if re.search(pattern, url):
        return re.sub(pattern, replacement, url)
    
    return url
