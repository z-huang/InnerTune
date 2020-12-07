package com.zionhuang.music.ui.fragments.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding
import java.lang.reflect.ParameterizedType

abstract class BindingFragment<T : ViewBinding> : Fragment() {
    protected lateinit var binding: T

    @Suppress("UNCHECKED_CAST")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        setHasOptionsMenu(true)
        val clazz = (javaClass.genericSuperclass as ParameterizedType).actualTypeArguments[0] as Class<T>
        binding = clazz.getMethod("inflate", LayoutInflater::class.java, ViewGroup::class.java, Boolean::class.java)
                .invoke(null, inflater, container, false) as T
        return binding.root
    }
}
