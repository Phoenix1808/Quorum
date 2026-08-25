package com.example.quorum.ui.feed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.quorum.data.model.Proposal
import com.example.quorum.data.repository.QuorumRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// TODO: FeedViewModel — proposals fetch + UI state (loading/success/error)

sealed interface FeedUiState{
    data object Loading : FeedUiState
    data class Success(val proposals: List<Proposal>) : FeedUiState
    data class Error(val message: String): FeedUiState
}

class FeedViewModel : ViewModel(){
    private val repository = QuorumRepository()

    private val _uistate = MutableStateFlow<FeedUiState>(FeedUiState.Loading)
    val uiState : StateFlow<FeedUiState> = _uistate.asStateFlow()

    init {
        loadProposals()
    }

    // spaces = comma-separated Snapshot space ids. null = backend ke default 3 DAOs.
    // Repository/Retrofit me ye param pehle se tha, bas Feed kabhi bhejta nahi tha.
    fun loadProposals(state:String?=null, spaces:String?=null){
        _uistate.value = FeedUiState.Loading
        viewModelScope.launch {
            try{
                val proposals = repository.getProposals(spaces=spaces, state=state)
                _uistate.value = FeedUiState.Success(proposals)
            } catch (e:Exception){
                _uistate.value = FeedUiState.Error(e.message?:"Something went wrong")
            }
        }
    }
}