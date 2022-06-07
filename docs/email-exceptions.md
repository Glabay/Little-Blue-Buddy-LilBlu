# Little Blue Buddy (Email & Templates)

<img height="164" src="images/lilblu-large.png" alt="Little Blue Buddy" width="164"/><img alt="Newfold Digital" height="123" src="images/newfold-asset-logo.png" width="474"/>

## Email Exceptions

####    Exceptions
When we have to flip into a code for some reason, this tool makes reporting the reason easy.
<br>Exception Date: taken from today's date
<br>Exception Start: Taken from the current time when the window was open
<br>Exception Finish: Optional, this will be autofilled when "_Send Exception_" is pressed if it was not specifically entered.
<br>Using _Special Project_ as the default, you can choose from any of the phone codes
<br>_Approved by_ is an optional field, but if you were approved, enter the name of the approving person here
<br>When opened the cursor will autofocus on the _Reason_ field, so you can just quickly open and provide a reason, then send the exception.
<br>Clicking the send exception button will send your exception to the appropriate department

![Exception-Email-Tool](images/exceptions/exception-emailer.png)

Once the email is sent, the receiving end will see the result (To & From emails were removed in the image)

![Exception-Email-Result](images/exceptions/exception-emailer-example.png)


####    Missed\Late Event
When reporting a missed Event, this tool makes sending the email very quick
<br>Event Date: taken from today's date
<br>Time Event was taken: Taken from the current time when the window was open
<br>Event Reporting:

![Event-Email-Options](images/exceptions/event-email-options.png)

![Event-Email](images/exceptions/event-email.png)


When reporting a Workday event the window changes a little to fit the template:

![Event-Email-Workday](images/exceptions/event-email-workday.png)

## Templates

####    Follow-Ups
When we make a callback, this window will open and the only this you need to enter is the Case you were following up for, and any reason that isn't an outbound call, with no answer, and you left a voicemail.
<br>The date is autofilled with today's date.
<br>The time is taken from the current time of day when the window was opened.
<br>You can select the day and attempt, once the call is complete, click "_Copy to Clipboard_" to have it stored for a quick Ctrl+V into Pega.

![Follow-Up-Template](images/exceptions/follow-up-template.png)

The Template pasted into the JotPad for an example of the format.
<br>The '_Agent_' is taken from the logged-in user of the Computer.

![Follow-Up-Text](images/exceptions/follow-up-template-text.png)


####    Scheduled Callback
When we have to make a callback, this is the window that will be displayed

![Scheduled-Callback-Blank](images/exceptions/scheduled-callback.png)

Once we fill in the required information and have the reasoning filled in.

![Scheduled-Callback-Blank](images/exceptions/scheduled-callback-filled.png)

The template copied to your clipboard or sent to Email

![Scheduled-Callback-Blank](images/exceptions/scheduled-callback-example.png)
