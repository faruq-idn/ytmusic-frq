package com.frq.ytmusic.domain.model

/**
 * Manages the playback queue.
 */
class QueueManager {
    private val _queue = mutableListOf<Song>()
    val queue: List<Song> get() = _queue.toList()
    
    private var _currentIndex = -1
    val currentIndex: Int get() = _currentIndex
    
    val currentSong: Song?
        get() = if (_currentIndex in _queue.indices) _queue[_currentIndex] else null
    
    val hasNext: Boolean
        get() = _currentIndex < _queue.size - 1
    
    val hasPrevious: Boolean
        get() = _currentIndex > 0
    
    /**
     * Set queue and optionally start at specific index.
     */
    fun setQueue(songs: List<Song>, startIndex: Int = 0) {
        _queue.clear()
        _queue.addAll(songs)
        _currentIndex = startIndex.coerceIn(0, _queue.size - 1)
    }
    
    /**
     * Add song to end of queue.
     */
    fun addToQueue(song: Song) {
        _queue.add(song)
        if (_currentIndex == -1) _currentIndex = 0
    }
    
    /**
     * Add multiple songs to queue.
     */
    fun addAllToQueue(songs: List<Song>) {
        _queue.addAll(songs)
        if (_currentIndex == -1 && _queue.isNotEmpty()) _currentIndex = 0
    }
    
    /**
     * Play next song.
     */
    fun next(): Song? {
        if (hasNext) {
            _currentIndex++
            return currentSong
        }
        return null
    }
    
    /**
     * Play previous song.
     */
    fun previous(): Song? {
        if (hasPrevious) {
            _currentIndex--
            return currentSong
        }
        return null
    }
    
    /**
     * Jump to specific index.
     */
    fun skipTo(index: Int): Song? {
        if (index in _queue.indices) {
            _currentIndex = index
            return currentSong
        }
        return null
    }
    
    /**
     * Remove song at index.
     */
    fun removeAt(index: Int) {
        if (index in _queue.indices) {
            _queue.removeAt(index)
            if (index < _currentIndex) {
                _currentIndex--
            } else if (index == _currentIndex && _currentIndex >= _queue.size) {
                _currentIndex = _queue.size - 1
            }
        }
    }
    
    /**
     * Clear queue.
     */
    fun clear() {
        _queue.clear()
        _currentIndex = -1
    }
}
