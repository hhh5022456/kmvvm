package com.catchpig.mvvm.base.view

import com.catchpig.mvvm.widget.refresh.RefreshRecyclerView
import kotlinx.coroutines.flow.Flow

interface BaseVMView : BaseView {
    fun initParam()

    fun initView()

    fun initFlow()

    @Deprecated("")
    fun <T> lifecycleFlowRefresh(flow: Flow<MutableList<T>>, refresh: RefreshRecyclerView)

    @Deprecated("")
    fun <T> lifecycleFlow(
        flow: Flow<T>,
        errorCallback: ((t: Throwable) -> Unit)? = null,
        callback: T.() -> Unit
    )

    @Deprecated("")
    fun <T> lifecycleFlowLoadingView(
        flow: Flow<T>,
        errorCallback: ((t: Throwable) -> Unit)? = null,
        callback: T.() -> Unit
    )

    @Deprecated("")
    fun <T> lifecycleFlowLoadingDialog(
        flow: Flow<T>,
        errorCallback: ((t: Throwable) -> Unit)? = null,
        callback: T.() -> Unit
    )
}