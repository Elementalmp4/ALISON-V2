package main.java.de.voidtech.alison.commands;

import java.util.List;

import main.java.de.voidtech.alison.entities.AlisonModel;
import main.java.de.voidtech.alison.entities.CommandContext;
import main.java.de.voidtech.alison.utils.PackManager;
import main.java.de.voidtech.alison.utils.PrivacyManager;
import main.java.de.voidtech.alison.utils.WebhookManager;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.Webhook;
import net.dv8tion.jda.api.utils.Result;

public class ImitateCommand extends AbstractCommand {

    @Override
    public void execute(CommandContext context, List<String> args) {
    	String ID;
    	if (args.isEmpty()) ID = context.getAuthor().getId();
    	else ID = args.get(0).replaceAll("([^0-9])", "");
    	
        if (!PackManager.packExists(ID)) {
        	context.reply("I couldn't find that user :(");
        	return;
        }
        
        if (PrivacyManager.userHasOptedOut(ID)) {
    		context.reply("This user has chosen not to be imitated.");
    		return;
    	}
        AlisonModel model = PackManager.getPack(ID);
        String msg = model.createSentence();
		if (msg == null) {
			context.reply("There's no data for this user!");
			return;
		}
        
        Result<User> userResult = context.getJDA().retrieveUserById(ID).mapToResult().complete();
        if (userResult.isSuccess()) {
        	Webhook hook = WebhookManager.getOrCreateWebhook(context.getMessage().getTextChannel(), "ALISON", context.getJDA().getSelfUser().getId());
        	WebhookManager.sendWebhookMessage(hook, msg, userResult.get().getName(), userResult.get().getAvatarUrl());
        } else {
        	if (model.hasMeta()) {
        		Webhook hook = WebhookManager.getOrCreateWebhook(context.getMessage().getTextChannel(), "ALISON", context.getJDA().getSelfUser().getId());
            	WebhookManager.sendWebhookMessage(hook, msg, model.getMeta().getName(), model.getMeta().getIconUrl());
        	} else context.reply(msg);	
        }
    }
    
    @Override
    public String getName() {
        return "imitate";
    }

	@Override
	public String getUsage() {
		return "imitate [user mention or ID]";
	}

	@Override
	public String getDescription() {
		return "Allows you to use the power of ALISON to imitate someone! ALISON constantly learns from your messages,"
				+ " and when you use this command, she uses her knowledge to try and speak like you do!\n\n"
				+ "To stop ALISON from learning from you, use the optout command!";
	}

	@Override
	public String getShorthand() {
		return "i";
	}

	@Override
	public boolean isDmCapable() {
		return false;
	}

	@Override
	public boolean requiresArguments() {
		return false;
	}
}
