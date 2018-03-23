package ch.adv.ui;

import ch.adv.ui.access.Parser;
import ch.adv.ui.access.Stringifyer;
import ch.adv.ui.presentation.Layouter;


public interface ADVModule {

    Layouter getLayouter();
    Stringifyer getStringifyer();
    Parser getParser();

}
