package net.kopeph.ld31.graphics;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import net.kopeph.ld31.InputHandler;
import net.kopeph.ld31.LD31;

public class HUD {
	//THESE ARE ALL THE USER MESSAGES
	public static final String MSG_FOOTER =
	"%s%s%s%s: Move %8s: Restart    Objective: Capture the Pink Square    Beware Of: White Light";
	public static final String MSG_FOOTER_END =
	    "           %8s: Restart";
	public static final String MSG_WIN = "YA DID IT!";
	public static final String MSG_DIE = "You ded Jim!"; //Sorry, this project is not MSG free
	
	private static String footerText = MSG_FOOTER, endFooterText = MSG_FOOTER_END, buildVersionText;
	static { buildVersionText = buildVersion(); }
	
	public static void render() {
		LD31 context = LD31.getContext();
		
		if (context.gameState() == LD31.ST_WIN || context.gameState() == LD31.ST_DIE) {
			context.renderer.font.render(endFooterText, 4, context.height - 12);
		} else if (context.gameState() == LD31.ST_RUNNING) {
			context.renderer.font.render(footerText, 4, context.height - 12);
		}
		
		context.renderer.font.render(buildVersionText, context.width - buildVersionText.length()*8 - 4, 4);
	}

	/** Updates the footer HUD text to reflect control bindings */
	public static void updateFooterText(InputHandler input) {
		footerText = String.format(HUD.MSG_FOOTER,
			InputHandler.getKeyIdString(input.getMainBindingFor(InputHandler.CTL_UP)),
			InputHandler.getKeyIdString(input.getMainBindingFor(InputHandler.CTL_LEFT)),
			InputHandler.getKeyIdString(input.getMainBindingFor(InputHandler.CTL_DOWN)),
			InputHandler.getKeyIdString(input.getMainBindingFor(InputHandler.CTL_RIGHT)),
			InputHandler.getKeyIdString(input.getMainBindingFor(InputHandler.CTL_RESET)));
		endFooterText = String.format(HUD.MSG_FOOTER_END,
			InputHandler.getKeyIdString(input.getMainBindingFor(InputHandler.CTL_RESET)));
	}

	private static String buildVersion() {
		try {
			return ResourceBundle.getBundle("version").getString("build.versionString"); //$NON-NLS-1$ //$NON-NLS-2$
		} catch (MissingResourceException e) {
			//If the version file doesn't exist, make a version string based on
			//branch name and short hash

			String branchName = "?"; //$NON-NLS-1$
			String branchHash = "?"; //$NON-NLS-1$
			try (BufferedReader gitHead = new BufferedReader(new FileReader(".git/HEAD"))) { //$NON-NLS-1$
				branchName = gitHead.readLine();
				branchName = branchName.substring(branchName.lastIndexOf('/') + 1);
				try (BufferedReader gitRefHead = new BufferedReader(new FileReader(".git/refs/heads/" + branchName))) { //$NON-NLS-1$
					branchHash = gitRefHead.readLine().substring(0, 7);
				} catch (IOException ew) {
					//Oops. Ignore
				}
			} catch (IOException ew) {
				//Oops. Ignore
			}

			return String.format("git %S.%s", branchName, branchHash); //$NON-NLS-1$
		}
	}
}
