package com.example.quorum.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.quorum.data.model.Proposal
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