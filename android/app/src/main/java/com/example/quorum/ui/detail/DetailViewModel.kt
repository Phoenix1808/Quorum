package com.example.quorum.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.quorum.data.model.Proposal
import com.example.quorum.data.remote.fetchUserVoteOn
import com.example.quorum.data.repository.QuorumRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// TODO: DetailViewModel — single proposal fetch by id + UI state

sealed interface DetailUiState{
    data object Loading: DetailUiState
    data class Success(val proposal: Proposal) : DetailUiState
    data class Error(val message: String) : DetailUiState
}

class DetailViewModel: ViewModel(){
    private val repository = QuorumRepository()
    private val _uiState = MutableStateFlow<DetailUiState>(DetailUiState.Loading)
    val uiState: StateFlow<DetailUiState> = _uiState.asStateFlow()

    // Is proposal par user ka pehle se pada vote (1-based choice), ya null.
    // Proposal ke UI state se alag rakha hai kyunki ye wallet address par depend karta
    // hai -- address proposal se independent badal sakta hai (connect/disconnect).
    private val _myVote = MutableStateFlow<Int?>(null)
    val myVote: StateFlow<Int?> = _myVote.asStateFlow()

    fun loadMyVote(address: String?, proposalId: String){
        if(address == null){
            _myVote.value = null
            return
        }
        viewModelScope.launch{
            _myVote.value = try {
                fetchUserVoteOn(address, proposalId)
            } catch (e: Exception){
                // network fail -> bas indicator na dikhao, screen mat todo
                null
            }
        }
    }

    fun loadProposal(id:String){
        _uiState.value = DetailUiState.Loading
        viewModelScope.launch{
            try{
                val proposal = repository.getProposal(id)
                _uiState.value = DetailUiState.Success(proposal)
            } catch (e:Exception){
                _uiState.value = DetailUiState.Error(e.message ?: "Something went wrong")
            }
        }
    }
}