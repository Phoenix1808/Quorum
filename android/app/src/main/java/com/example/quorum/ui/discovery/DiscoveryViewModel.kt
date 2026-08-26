package com.example.quorum.ui.discovery

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.quorum.data.model.DaoItem
import com.example.quorum.data.remote.WalletSession
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

    init {
        // Pehle yahan "demo-user" hardcoded tha -- matlab har wallet ke follows Redis me
        // ek hi bucket (user:demo-user:follows) me jate the. Ab asli address se bandhe hain.
        // address badalne par (connect/disconnect/switch) list dobara load karni padti hai,
        // kyunki follows us address ke sath jude hote hain.
        viewModelScope.launch {
            WalletSession.address.collect { loadDaos() }
        }
    }

    fun loadDaos(){
        val address = WalletSession.address.value
        _uiState.value = DiscoveryUiState.Loading
        viewModelScope.launch{
            try{
                val daos = repos.getDaos()
                // Wallet connected nahi -> DAO list to dikhao, par follows kiske? Kisi ke nahi.
                val followed = if (address != null) repos.getFollows(address).toSet() else emptySet()
                _uiState.value = DiscoveryUiState.Success(daos,followed)
            } catch (e:Exception){
                _uiState.value = DiscoveryUiState.Error(e.message ?: "Something Went Wrong")
            }
        }
    }

    fun toggleFollow(daoId:String){
        val current = _uiState.value
        if(current !is DiscoveryUiState.Success) return
        // Wallet ke bina follow kiske naam pe hoga? Isliye chup-chaap kuch mat karo.
        val address = WalletSession.address.value ?: return
        viewModelScope.launch{
            try{
                val updated = if (daoId in current.followed){
                    repos.unfollow(address,daoId)
                } else{
                    repos.follow(address,daoId)
                }
                _uiState.value = current.copy(followed = updated.toSet())

            } catch (e:Exception){
                //silenlty ignore error
            }
        }
    }
}