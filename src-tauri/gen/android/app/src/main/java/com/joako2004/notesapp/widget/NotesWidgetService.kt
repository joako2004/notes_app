package com.joako2004.notesapp.widget

import android.content.Intent
import android.widget.RemoteViewsService

class NotesWidgetService : RemoteViewsService() {
    override fun onGetViewFactory(intent: Intent): RemoteViewsService.RemoteViewsFactory =
        NotesWidgetFactory(applicationContext)
}
