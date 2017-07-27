package za.co.tagtron.tagtronuhf;

import android.content.Context;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.ListView;

public class NonScrollingListView extends ListView {

	public NonScrollingListView(Context context) {
        super(context);
    }
    public NonScrollingListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
	
    public NonScrollingListView(Context context, AttributeSet attrs, int defaultStyle) {
        super(context, attrs, defaultStyle);
    }

    @Override
    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // HACK!  TAKE THAT ANDROID!
        // Calculate entire height by providing a very large height hint.
        // View.MEASURED_SIZE_MASK represents the largest height possible.
        int expandSpec = MeasureSpec.makeMeasureSpec(MEASURED_SIZE_MASK, MeasureSpec.AT_MOST);
        super.onMeasure(widthMeasureSpec, expandSpec);

        ViewGroup.LayoutParams params = getLayoutParams();
        params.height = getMeasuredHeight();
    }
}