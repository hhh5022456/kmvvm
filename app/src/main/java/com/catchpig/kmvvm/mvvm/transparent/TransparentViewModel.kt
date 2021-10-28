package com.catchpig.kmvvm.mvvm.transparent

import androidx.lifecycle.MutableLiveData
import com.catchpig.kmvvm.entity.Banner
import com.catchpig.kmvvm.repository.WanAndroidRepository
import com.catchpig.mvvm.base.viewmodel.BaseViewModel
import com.catchpig.mvvm.ext.success
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class TransparentViewModel : BaseViewModel() {
    private val wanAndroidProxy = WanAndroidRepository
    val bannerLiveData = MutableLiveData<Banner>()

    fun getBanner() {
        wanAndroidProxy.getBanners().success({
            bannerLiveData.value = it
        })
    }

    suspend fun banner(): Flow<Banner> {
        return flow {
            emit(wanAndroidProxy.queryBanner())
        }
    }
}