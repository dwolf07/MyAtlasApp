package com.layer.atlas;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.layer.sdk.LayerClient;
import com.layer.sdk.listeners.LayerTypingIndicatorListener;
import com.layer.sdk.messaging.Conversation;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * AtlasTypingIndicator provides feedback about typists within a Conversation.  When initialized
 * and registered with a LayerClient as a LayerTypingIndicatorListener, AtlasTypingIndicator
 * maintains a set of typists for the given Conversation, providing callbacks when UI updates are
 * needed.  AtlasTypingIndicator can also listen to all Conversations, and provides a default UI
 * updater if desired.  See initialize() for details.
 */
public class AtlasTypingIndicator extends FrameLayout implements LayerTypingIndicatorListener {
    private Conversation mConversation;
    private final Set<String> mTypists = new HashSet<String>();
    private TextView mTextView;
    private Callback mCallback;

    // styles
    private int mTextColor;
    private Typeface mTextTypeface;
    private int mTextStyle;
    private float mTextSize;

    public AtlasTypingIndicator(Context context) {
        super(context);
    }

    public AtlasTypingIndicator(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AtlasTypingIndicator(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        parseStyle(context, attrs, defStyle);
    }

    /**
     * Initializes this AtlasTypingIndicator.  Pass in a Conversation to listen for typing on that
     * Conversation alone, or `null` for all Conversations.  Pass in a Callback to handle styling
     * externally; a default implementation is provided by DefaultTypingIndicatorCallback.
     *
     * @param conversation Conversation in which to listen for typing, or `null` for all typing.
     * @param callback     Callback for responding to typing.
     * @return This AtlasTypingIndicator for chaining.
     */
    public AtlasTypingIndicator init(Conversation conversation, Callback callback) {
        if (callback == null) throw new IllegalArgumentException("Callback cannot be null");
        mConversation = conversation;
        mCallback = callback;
        mTextView = new TextView(getContext());
        addView(mTextView);
        applyStyle();
        return this;
    }

    private void parseStyle(Context context, AttributeSet attrs, int defStyle) {
        TypedArray ta = context.getTheme().obtainStyledAttributes(attrs, R.styleable.AtlasTypingIndicator, R.attr.AtlasTypingIndicator, defStyle);
        Resources r = context.getResources();
        mTextColor = ta.getColor(R.styleable.AtlasTypingIndicator_textColor, r.getColor(R.color.atlas_text_black));
        mTextStyle = ta.getInt(R.styleable.AtlasTypingIndicator_textStyle, Typeface.NORMAL);
        mTextTypeface = Typeface.create(ta.getString(R.styleable.AtlasParticipantPicker_inputTextTypeface), mTextStyle);
        mTextSize = ta.getDimension(R.styleable.AtlasTypingIndicator_textSize, r.getDimension(R.dimen.atlas_text_size_general));
        ta.recycle();
    }

    private void applyStyle() {
        mTextView.setTextColor(mTextColor);
        mTextView.setTypeface(mTextTypeface);
        //mTextView.setTextSize(mTextSize);
    }

    /**
     * Clears the current list of typists and calls refresh().
     *
     * @return This AtlasTypingIndicator for chaining.
     */
    public AtlasTypingIndicator clear() {
        synchronized (mTypists) {
            mTypists.clear();
        }
        refresh();
        return this;
    }

    /**
     * Calls Callback.onTypingUpdate() with the current list of typists.
     *
     * @return This AtlasTypingIndicator for chaining.
     */
    private AtlasTypingIndicator refresh() {
        synchronized (mTypists) {
            mCallback.onTypingUpdate(this, mTypists);
        }
        return this;
    }

    /**
     * Sets the text content of this AtlasTypingIndicator.
     *
     * @param text Text content
     */
    public void setText(CharSequence text) {
        mTextView.setText(text);
    }

    @Override
    public void onTypingIndicator(LayerClient layerClient, Conversation conversation, String userId, TypingIndicator typingIndicator) {
        if ((mConversation != null) && (mConversation != conversation)) return;
        if (typingIndicator == TypingIndicator.FINISHED) {
            mTypists.remove(userId);
        } else {
            mTypists.add(userId);
        }
        refresh();
    }

    /**
     * AtlasTypingIndicator.Callback allows an external class to set indicator text, visibility,
     * etc. based on the current typists.
     */
    public interface Callback {
        /**
         * Notifies the callback to typist updates.
         *
         * @param indicator     The AtlasTypingIndicator notifying
         * @param typingUserIds The set of currently-active typist user IDs
         */
        void onTypingUpdate(AtlasTypingIndicator indicator, Set<String> typingUserIds);
    }

    /**
     * Default Callback handler implementation.
     */
    public static class DefaultTypingIndicatorCallback implements Callback {
        private final Atlas.ParticipantProvider mParticipantProvider;

        public DefaultTypingIndicatorCallback(Atlas.ParticipantProvider participantProvider) {
            mParticipantProvider = participantProvider;
        }

        @Override
        public void onTypingUpdate(AtlasTypingIndicator indicator, Set<String> typingUserIds) {
            List<Atlas.Participant> typists = new ArrayList<Atlas.Participant>(typingUserIds.size());
            for (String userId : typingUserIds) {
                Atlas.Participant participant = mParticipantProvider.getParticipant(userId);
                if (participant != null) typists.add(participant);
            }

            if (typists.isEmpty()) {
                indicator.setText(null);
                indicator.setVisibility(GONE);
                return;
            }

            String[] strings = indicator.getResources().getStringArray(R.array.atlas_typing_indicator);
            String string = strings[Math.min(strings.length - 1, typingUserIds.size())];
            String[] names = new String[typists.size()];
            int i = 0;
            for (Atlas.Participant typist : typists) {
                names[i++] = Atlas.getFullName(typist);
            }
            indicator.setText(String.format(string, names));
            indicator.setVisibility(VISIBLE);
        }
    }

}
