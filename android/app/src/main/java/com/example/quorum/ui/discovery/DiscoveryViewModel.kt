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
    data class Success(val daos: List<DaoItem>,val followed: Set<String>): DiscoveryUiState
    data class Error(val message:String) : DiscoveryUiState
}

class DiscoveryViewModel : ViewModel(){
    private val repos = QuorumRepository()
    private val _uiState = MutableStateFlow<DiscoveryUiState>(DiscoveryUiState.Loading)
    val uiState : StateFlow<DiscoveryUiState> = _uiState.asStateFlow()
    private val userAd = "demo-user"

    init {loadDaos()}


fun loadDaos(){
    _uiState.value = DiscoveryUiState.Loading
    viewModelScope.launch{
        try{
            val daos = repos.getDaos()
            val followed = repos.getFollows(userAd).toSet()
            _uiState.value = DiscoveryUiState.Success(daos,followed)
        } catch (e:Exception){
            _uiState.value = DiscoveryUiState.Error(e.message ?: "Something Went Wrong")
        }
    }
}

    fun toggleFollow(daoId:String){
        val current = _uiState.value
        if(current !is DiscoveryUiState.Success) return
        viewModelScope.launch{
            try{
                val updated = if (daoId in current.followed){
                    repos.unfollow(userAd,daoId)
                } else{
                    repos.follow(userAd,daoId)
                }
                _uiState.value = current.copy(followed = updated.toSet())

            } catch (e:Exception){
                //silenlty ignore error
            }
        }
    }
}