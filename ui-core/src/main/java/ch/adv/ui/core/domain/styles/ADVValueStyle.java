package ch.adv.ui.core.domain.styles;


/**
 * ADVStyle implementation that uses 'low level' values.
 */
public class ADVValueStyle implements ADVStyle {
    private int fillColor;
    private int strokeColor;
    private ADVStrokeStyle strokeStyle;
    private int strokeThickness;

    @Override
    public int getFillColor() {
        return fillColor;
    }


    @Override
    public int getStrokeColor() {
        return strokeColor;
    }

    @Override
    public String getStrokeStyle() {
        if (strokeStyle == null) {
            return ADVStrokeStyle.NONE.getStyle();
        } else {
            return strokeStyle.getStyle();
        }
    }

    public void setStrokeStyle(ADVStrokeStyle strokeStyle) {
        this.strokeStyle = strokeStyle;
    }

    @Override
    public int getStrokeThickness() {
        return strokeThickness;
    }
}