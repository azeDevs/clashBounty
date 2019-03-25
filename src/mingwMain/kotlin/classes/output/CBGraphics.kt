package classes.output

import classes.session.ClashBountySession
import libui.ktx.*


fun displayAppWindow() = appWindow("gearNet: Clash Bounty", 320, 240) {
    vbox { lateinit var scroll: TextArea

        button("Connect to Guilty Gear Xrd") { action {
            ClashBountySession().start()
            scroll.value = consoleLogs.toString().trimMargin()
        } }

        scroll = textarea { readonly = true; stretchy = true; value = consoleLogs.toString().trimMargin() }

        button("REFRESH") { action { scroll.value = consoleLogs.toString().trimMargin() } }

    }
}

