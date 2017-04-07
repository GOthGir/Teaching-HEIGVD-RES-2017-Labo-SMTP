## What is MailBomb Boom Go ?
MailBomb Boom Go is a Java client that offer the possibility to play a "Prank Campaign" by sending forged emails to a list of victims defined by the user.

You can create a group of "victims" you want to prank and create predefined "prank texts" you want to send to them.

## How it works ?
MailBomb Boom Go use the SMTP protocol to send emails by using a victim email address to a group of receiver victims. It send a "forged" mail. It means that the sender of the message will be the real email address of the person and the receiver will really believe that it is the real sender.

You only have to create two files, a "victims" file and a "pranks" file, start the program by giving the files, the SMTP server address and enjoy !

## How can we "steal" the sender email address ?
The SMTP protocol works like this:
1. `Client: EHLO`: the client send a message to the server to indicate that he want to send a message.
2. `Server: 250 INFOS`: the server gives different informations about the data size.
3. `Client: MAIL FROM <sender@xxxx.xxxx>`: the client indicate the sender email address.
4. `Server: 250 Sender ok`: the server accepts the sender.
5. `Client: RCPT TO: <receiver@yyyy.yyyy>`: the client send the receiver email address.
6. `Server: 250 Recipient ok.`: the server accepts the receiver.
7. `Client: DATA`: the client start to send the mail content.
8. `Server: 354 Enter mail, end with "." on a line by itself`: the server indicates how to send the data.
9. `Client: Subject: Test`: the mail subject.
10. `Client: Message Text`: the mail content.
11. `Client: .`: an unique point indicate the end of data of the mail content.
12. `Server: 250 Ok`: accepts the mail content.
13. `Client: QUIT`: the client finish the communication.
14. `Server: 221 Closing connection`: the server close the connection.

When you have understood how the SMTP communication works, it is very simple to write a program that send the correct commands and the data you want. The interesting point is the 3rd where you specifies the mail sender. You can indicate any sender you want !

Another specification of the SMTP protocol important to understand is how we know when the server have finished to send responses to the client. If we take a multiple line response example :
```
250-smtp.xxxx.xxxx
250-PIPELINING
250 8BITMIME
```
We see that the last line is the only one that contain a space separator after the response code. It's the only way we found to know if the server finished to send his responses.

It is important to understand how the server interprets an end of line sent by the client. The SMTP take the `\n\r` end of line separator.

## How to use MailBomb Boom Go ?
First, you have to download the executable JAR file. Then you have to create the victims and pranks files by following these formats :

**The victims file must be like this :**
```
{"senders":["salut1@yoyo.com","salut2@yoyo.com","salut3@yoyo.com","salut4@yoyo.com"],"receivers":["ayeaye1@yoyo.com","ayeaye2@yoyo.com","ayeaye3@yoyo.com","ayeaye4@yoyo.com"]}!$&#%#&$!
{"senders":["blabla@yoyo.com"],"receivers":["gruetzy@yoyo.com","salut@yoyo.com"]}!$&#%#&$!
```
If you know what it means, the victims file simply contains JSON objects. A senders/receivers group is surrounded by braces `{}` and contains `"senders":arrayOfSenders,"receivers":arrayOfReceivers"`. The senders array must have this form `["sender1","sender2",...]` and the receivers must have the same form `["receiver1","receiver2",...]`. If you want to create several senders/receivers groups, you have to separate the groups with the specified separator `!$&#%#&$!`.

**The pranks file must be like this:**
```
C'est deux spermatozoïdes qui discutent :
- Dis, c'est encore loin les ovaires ?
- Tu parles, on n'est qu'aux amydales.
!$&#%#&$!
- Dis maman, un citron, ça a des pattes ?
- ???
- Dis maman, un citron, ça a des pattes ?
- Euh... ben non, un citron ça n'a pas de pattes.
- Ah ben c'est un poussin que j'ai pressé, alors.
!$&#%#&$!
```
The file simply contains the prank texts separated with the `!$&#%#&$!` separator.

When you have created your two files you have two options to run the program :

**Basic usage:**

You have to place the two files named `victims.lol` and `pranks.lol` in the same directory as the program and simply run the JAR executable. It will try to connect to a smtp server hosted locally (localhost) listening on port 2525.

**Advanced usage:**
- `-victims "<pathToFile>"`: the path to your victims file.
- `-pranks "<pathToFile>"`: the path to your pranks file.
- `-host <serverAddress>`: the SMTP server host you want to use. (default : localhost)
- `-port <portNumber>`: the port used in the SMTP server host (default : 25).
