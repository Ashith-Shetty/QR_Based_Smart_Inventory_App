package com.yourname.qrinventory.utils;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.Toast;
import androidx.core.content.FileProvider;
import java.io.File;

public class ShareUtils {
    private static Uri uriFor(Context ctx, File file) {
        return FileProvider.getUriForFile(ctx, ctx.getPackageName() + ".fileprovider", file);
    }

    // Opens the Android share sheet (implicit intent) so the user can send the file anywhere
    public static void shareFile(Context ctx, File file, String mimeType, String subject) {
        Intent send = new Intent(Intent.ACTION_SEND);
        send.setType(mimeType);
        send.putExtra(Intent.EXTRA_STREAM, uriFor(ctx, file));
        send.putExtra(Intent.EXTRA_SUBJECT, subject);
        send.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        Intent chooser = Intent.createChooser(send, "Share via");
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        ctx.startActivity(chooser);
    }

    // Opens the file in an installed viewer app (e.g. a PDF viewer)
    public static void viewFile(Context ctx, File file, String mimeType) {
        Intent view = new Intent(Intent.ACTION_VIEW);
        view.setDataAndType(uriFor(ctx, file), mimeType);
        view.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_ACTIVITY_NEW_TASK);
        try {
            ctx.startActivity(view);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(ctx, "No app found to open this file", Toast.LENGTH_SHORT).show();
        }
    }
}
