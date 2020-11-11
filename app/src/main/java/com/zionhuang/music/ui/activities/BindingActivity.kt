package com.zionhuang.music.ui.activities

import android.os.Bundle
import android.view.LayoutInflater
import androidx.annotation.CallSuper
import androidx.appcompat.app.AppCompatActivity
import androidx.viewbinding.ViewBinding
import java.lang.reflect.ParameterizedType

abstract class BindingActivity<T : ViewBinding> : AppCompatActivity() {
    protected lateinit var binding: T

    @Suppress("UNCHECKED_CAST")
    @CallSuper
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val clazz = (javaClass.genericSuperclass as ParameterizedType).actualTypeArguments[0] as Class<T>
        binding = clazz.getMethod("inflate", LayoutInflater::class.java)
                .invoke(null, layoutInflater) as T
        setContentView(binding.root)
    }
}