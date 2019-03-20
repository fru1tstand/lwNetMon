package me.fru1t.lwnetmon.dagger

import dagger.Component
import me.fru1t.lwnetmon.LwNetMonApplication

/** Sole dagger entry point for this application. */
@Component(modules = [ServerModule::class])
interface LwNetMonComponent {
  fun inject(lwNetMonApplication: LwNetMonApplication)
}
