package com.example.quorum.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.quorum.data.model.VoteHistory
import com.example.quorum.data.remote.fetchUserVotes
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface HistoryUiState{
    data object Loading : HistoryUiState
    data class Success(val votes : List<VoteHistory>):HistoryUiState
    data object Empty: HistoryUiState
    data class Error(val message : String): HistoryUiState
}

class HistoryViewModel: ViewModel(){
    private val _uiState = MutableStateFlow<HistoryUiState>(HistoryUiState.Loading)
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

    fun loadVotes(address:String){
        _uiState.value = HistoryUiState.Loading
        viewModelScope.launch{
            try{
              val votes = fetchUserVotes(address)
              _uiState.value = if(votes.isEmpty()) HistoryUiState.Empty
              else HistoryUiState.Success(votes)
            } catch(e:Exception){
             _uiState.value = HistoryUiState.Error(e.message ?: "Failed to Load Votes")
            }
        }
    }
}