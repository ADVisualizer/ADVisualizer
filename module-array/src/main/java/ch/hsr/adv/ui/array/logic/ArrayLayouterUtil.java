package ch.hsr.adv.ui.array.logic;

import ch.hsr.adv.ui.core.logic.domain.styles.ADVStyle;
import ch.hsr.adv.ui.core.presentation.util.StyleConverter;
import ch.hsr.adv.ui.core.presentation.widgets.LabeledNode;
import com.google.inject.Singleton;
import javafx.scene.paint.Color;

/**
 * Common layouter methods
 */
@Singleton
public class ArrayLayouterUtil {

    /**
     * Sets the styling for a array node
     *
     * @param node  array node
     * @param style style
     */
    public void setStyling(LabeledNode node, ADVStyle style) {
        Color fillColor = StyleConverter
                .getColorFromHexValue(style.getFillColor());

        node.setBackgroundColor(fillColor);
        node.setFontColor(StyleConverter.getLabelColor(fillColor));
        node.setBorder(style.getStrokeThickness(),
                StyleConverter.getColorFromHexValue(style.getStrokeColor()),
                StyleConverter.getStrokeStyle(style.getStrokeStyle()));
    }
}
