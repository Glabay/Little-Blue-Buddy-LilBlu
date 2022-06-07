# Little Blue Buddy (DNS-Panel)

<img height="164" src="images/lilblu-large.png" alt="Little Blue Buddy" width="164"/><img alt="Newfold Digital" height="123" src="images/newfold-asset-logo.png" width="474"/>


## Document Hound
Here you are greeted with a window where you can enter the domain you will to sniff out.

![DocuHound-Empty](images/docuhound/DocuHound-Blank.png)

Once a domain is provided, and you "_Release the hounds_", the pack leader will sniff out the robots.txt file for a sitemap link. If none is found we will then try to look at the domain/sitemap.xml
If a map is found then we will look over the sitemap links for anything that is a direct link to a file type we are looking for, else it will add to the list of links to assign out.
Once the sitemap has been sniffed and filtered the leader will assign a hound to run over each page for any href (hyperlink reference) that points to a file type we are looking for and stores it to pass back to the pack leader once the page is sniffed.
Once all the hounds have reported back to the leader with their findings the leader reports the findings back to the user in an informative manner.

![DocuHound-Populated](images/docuhound/DocuHound-Result.png)

When we "_Save Documents_" we will be creating a folder in the download folder of the Agent's computer, and naming it the domain; replacing the '_dot_' with a dash.

![DocuHound-Saving](images/docuhound/DocuHound-saving.png)

If there are any Embedded videos or links to YouTube videos/channels will be saved to a text file '_LilBlu_Links.txt_' within that file

## Filter

![DocuHound-Filter-Options](images/docuhound/DocuHound-filter.png)

The filter will search for document types based on the filter applied.

- Word Files
    - *.docx
    - *.doc
    - *.docm


- PowerPoint
    - *.ppt
    - *.pptx
    - *.potx
  

- Video Links
    - YouTube.com/user/*
    - YouTube.com/c/*
    - YouTube.com/watch/*
    - *.mov
    - *.mp4
    - *.avi


<a href="https://glabay.github.io/Little-Blue-Buddy-LilBlu" target="_blank">Main Page</a>