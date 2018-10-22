package no.siriuslabs.image;

import eu.webtoolkit.jwt.Icon;
import eu.webtoolkit.jwt.Signal;
import eu.webtoolkit.jwt.StandardButton;
import eu.webtoolkit.jwt.WApplication;
import eu.webtoolkit.jwt.WContainerWidget;
import eu.webtoolkit.jwt.WEnvironment;
import eu.webtoolkit.jwt.WGridLayout;
import eu.webtoolkit.jwt.WLineEdit;
import eu.webtoolkit.jwt.WMessageBox;
import eu.webtoolkit.jwt.WPushButton;
import eu.webtoolkit.jwt.WText;

import java.util.EnumSet;

public class FrontendApplication extends WApplication {

	public FrontendApplication(WEnvironment env) {
		super(env);

		setTitle("Geoscience Image Classification");


		WContainerWidget readContainer = new WContainerWidget();

		WPushButton readButton = new WPushButton("Read from server");
		readContainer.addWidget(readButton);
		readButton.clicked().addListener(this, new Signal.Listener() {
			@Override
			public void trigger() {
				// TODO read from server

				WMessageBox box = new WMessageBox("Message", "Read something from server", Icon.NoIcon, EnumSet.of(StandardButton.Ok));
				box.buttonClicked().addListener(box, new Signal.Listener() {
					@Override
					public void trigger() {
						box.remove();
					}
				});
				box.show();
			}
		});


		WContainerWidget sendContainer = new WContainerWidget();

		WText tripletLabel = new WText("Triplets");
		sendContainer.addWidget(tripletLabel);
		WLineEdit tripletText1 = new WLineEdit();
		sendContainer.addWidget(tripletText1);
		WLineEdit tripletText2 = new WLineEdit();
		sendContainer.addWidget(tripletText2);
		WLineEdit tripletText3 = new WLineEdit();
		sendContainer.addWidget(tripletText3);

		WPushButton sendButton = new WPushButton("Send to server");
		sendContainer.addWidget(sendButton);
		sendButton.clicked().addListener(this, new Signal.Listener() {
			@Override
			public void trigger() {
				String text1 = tripletText1.getText().trim();
				String text2 = tripletText2.getText().trim();
				String text3 = tripletText3.getText().trim();

				// TODO send to server

				String fieldContents = text1 + ", " + text2 + ", " + text3;
				WMessageBox box = new WMessageBox("Message", "Send something to server: " + fieldContents, Icon.NoIcon, EnumSet.of(StandardButton.Ok));
				box.buttonClicked().addListener(box, new Signal.Listener() {
					@Override
					public void trigger() {
						box.remove();
					}
				});
				box.show();
			}
		});

		WGridLayout layout = new WGridLayout();
		layout.addWidget(readContainer, 0, 0);
		layout.addWidget(sendContainer, 0, 1);

		WContainerWidget root = getRoot();
		root.setLayout(layout);
	}
}
