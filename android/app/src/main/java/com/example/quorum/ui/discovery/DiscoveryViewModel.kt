package com.example.quorum.ui.discovery

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.quorum.data.model.DaoItem
import com.example.quorum.data.repository.QuorumRepository
import com.example.quorum.ui.theme.QuorumTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// TODO: DiscoveryViewModel — DAO list fetch + follow/unfollow state
sealed interface DiscoveryUiState{
    data object Loading : DiscoveryUiState
    data class Success(val daos: List<DaoItem>): DiscoveryUiState
    data class Error(val message:String) : DiscoveryUiState
}

class DiscoveryViewModel : ViewModel(){
    private val repos = QuorumRepository()
    private val _uiState = MutableStateFlow<DiscoveryUiState>(DiscoveryUiState.Loading)
    val uiState : StateFlow<DiscoveryUiState> = _uiState.asStateFlow()

    init {loadDaos()}


fun loadDaos(){
    _uiState.value = DiscoveryUiState.Loading
    viewModelScope.launch{
        try{
            _uiState.value = DiscoveryUiState.Success(repos.getDaos())
        } catch (e:Exception){
            _uiState.value = DiscoveryUiState.Error(e.message ?: "Something Went Wrong")
        }

    }
}
}