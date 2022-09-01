package com.zionhuang.music.ui.fragments

import android.os.Bundle
import android.view.*
import androidx.annotation.MenuRes
import androidx.core.os.bundleOf
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.zionhuang.innertube.models.ArtistItem
import com.zionhuang.innertube.models.PlaylistItem
import com.zionhuang.innertube.models.YTItem
import com.zionhuang.music.R
import com.zionhuang.music.databinding.MenuBottomSheetDialogBinding
import com.zionhuang.music.utils.NavigationEndpointHandler
import java.io.Serializable

typealias MenuModifier = Menu.() -> Unit
typealias MenuItemClickListener = (MenuItem) -> Unit

class MenuBottomSheetDialogFragment : BottomSheetDialogFragment() {
    private lateinit var binding: MenuBottomSheetDialogBinding

    @MenuRes
    private var menuResId: Int = 0
    private var menuModifier: MenuModifier? = null
    private var onMenuItemClicked: MenuItemClickListener? = null

    @Suppress("UNCHECKED_CAST")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        menuResId = requireArguments().getInt(KEY_MENU_RES_ID, 0)
        menuModifier = requireArguments().getSerializable(KEY_MENU_MODIFIER) as? MenuModifier
        onMenuItemClicked = requireArguments().getSerializable(KEY_MENU_LISTENER) as? MenuItemClickListener
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = MenuBottomSheetDialogBinding.inflate(layoutInflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.navigationView.background = binding.dragHandle.background
        binding.navigationView.apply {
            inflateMenu(menuResId)
            menuModifier?.invoke(menu)
            setNavigationItemSelectedListener {
                onMenuItemClicked?.invoke(it)
                dismiss()
                true
            }
        }
    }

    fun setMenuModifier(menuModifier: MenuModifier): MenuBottomSheetDialogFragment {
        requireArguments().putSerializable(KEY_MENU_MODIFIER, menuModifier as Serializable)
        return this
    }

    fun setOnMenuItemClickListener(listener: MenuItemClickListener): MenuBottomSheetDialogFragment {
        requireArguments().putSerializable(KEY_MENU_LISTENER, listener as Serializable)
        return this
    }

    companion object {
        private const val KEY_MENU_RES_ID = "MENU_RES_ID"
        private const val KEY_MENU_MODIFIER = "MENU_MODIFIER"
        private const val KEY_MENU_LISTENER = "LISTENER"

        fun newInstance(@MenuRes menuResId: Int) = MenuBottomSheetDialogFragment().apply {
            arguments = bundleOf(KEY_MENU_RES_ID to menuResId)
        }

        fun newInstance(item: YTItem, navigationEndpointHandler: NavigationEndpointHandler) = newInstance(R.menu.youtube_item)
            .setMenuModifier {
                findItem(R.id.action_radio)?.isVisible = item.menu.radioEndpoint != null
                findItem(R.id.action_play_next)?.isVisible = item.menu.playNextEndpoint != null
                findItem(R.id.action_add_to_queue)?.isVisible = item.menu.addToQueueEndpoint != null
                findItem(R.id.action_add_to_library)?.isVisible = item !is ArtistItem
                findItem(R.id.action_import_playlist)?.isVisible = item is PlaylistItem
                findItem(R.id.action_add_to_playlist)?.isVisible = item !is ArtistItem
                findItem(R.id.action_download)?.isVisible = item !is ArtistItem
                findItem(R.id.action_view_artist)?.isVisible = item.menu.artistEndpoint != null
                findItem(R.id.action_view_album)?.isVisible = item.menu.albumEndpoint != null
            }
            .setOnMenuItemClickListener { menuItem ->
                when (menuItem.itemId) {
                    R.id.action_radio -> navigationEndpointHandler.handle(item.menu.radioEndpoint)
                    R.id.action_play_next -> navigationEndpointHandler.playNext(item)
                    R.id.action_add_to_queue -> navigationEndpointHandler.addToQueue(item)
                    R.id.action_add_to_library -> navigationEndpointHandler.addToLibrary(item)
                    R.id.action_import_playlist -> if (item is PlaylistItem) {
                        navigationEndpointHandler.importPlaylist(item)
                    }
                    R.id.action_add_to_playlist -> navigationEndpointHandler.addToPlaylist(item)
                    R.id.action_download -> {}
                    R.id.action_view_artist -> navigationEndpointHandler.handle(item.menu.artistEndpoint)
                    R.id.action_view_album -> navigationEndpointHandler.handle(item.menu.albumEndpoint)
                    R.id.action_share -> navigationEndpointHandler.share(item)
                }
            }
    }
}