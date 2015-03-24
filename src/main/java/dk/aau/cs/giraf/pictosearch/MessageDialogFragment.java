package dk.aau.cs.giraf.pictosearch;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;

/**
 * Used to display a message to the user. Message is included in constructor
 */
@SuppressLint("ValidFragment")
public class MessageDialogFragment extends DialogFragment {
	private final String message;

    /**
     * Set this.message to message
     * @param message input string for dialog message
     */
	public MessageDialogFragment(String message) {
		this.message = message;
	}

    /**
     *
     * @param savedInstanceState A mapping from String values to various Parcelable types.
     * @return the AlertDialog object
     */
	@Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(message)
			.setPositiveButton("Ok", new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				//Do nothing
			}
		});
        return builder.create();
    }
}
