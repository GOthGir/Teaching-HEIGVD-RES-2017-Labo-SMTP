package ch.heig.fonkygati;

import java.io.*;
import java.net.Socket;
import java.util.*;

public class OurSmtpClient
{
   private final static String CLIENT_P = "Client  ~~o ";
   private final static String SERVER_P = "Serveur ~~o ";

   private final static String END_OF_DATA_LOL = "!$&#%#&$!";

   private final static String END_OF_DATA = "\r\n.\r\n";

   private static boolean crlf_error = false;

   private static String name = "fonkygati";
   private static String host = "localhost";
   private static int port = 2525;

   private static HashSet<Group> groups;
   private static HashSet<String> pranks;

   private static Socket socket;

   private static SmtpWriter writer;
   private static BufferedReader reader;

   private static Random rand;


   public static void main(String[] args)
   {
      // Initalize random to choose random pranks
      rand = new Random();

      // We collect each arguments and do the corresponding action if they are correct
      for (int i = 0; i < args.length; ++i)
      {
         BufferedReader fr = null;
         try
         {
            // If there is another argument after this one, we
            if(args.length > (i + 1))
            {
               fr = new BufferedReader(new FileReader(args[i + 1]));
            }

            // We get the current argument
            String command = args[i];
            System.out.println(command);

            // If the argument is the victims file, we have to read the corresponding file and get the victims
            if (command.equals("-victims") && args.length > (i + 1))
            {
               groups = new HashSet<Group>();
               String jsonGroupLine;
               String jsonGroup = "";
               // For each line from the file, collect the victims' mail
               while ((jsonGroupLine = fr.readLine()) != null)
               {
                  // We clean the line by removing the END_OF_DATA_LOL separator
                  String[] cleanLine = jsonGroupLine.split(END_OF_DATA_LOL);
                  if(cleanLine.length > 0)
                  {
                     jsonGroup += cleanLine[0];
                  }

                  // We 
                  if(jsonGroupLine.indexOf(END_OF_DATA_LOL) != -1)
                  {
                     groups.add(JsonObjectMapper.parseJson(jsonGroup, Group.class));
                     jsonGroup = "";
                  }
               }
               i++;
            } else if (command.equals("-pranks") && args.length > (i + 1))
            {
               pranks = new HashSet<String>();
               String prankLine;
               String prank = "";
               while ((prankLine = fr.readLine()) != null)
               {
                  String[] cleanLine = prankLine.split(END_OF_DATA_LOL);
                  if(cleanLine.length > 0 && !cleanLine[0].equals(END_OF_DATA_LOL))
                  {
                     prank += cleanLine[0] + "\r\n";
                  }
                  else
                  {
                     pranks.add(prank);
                     prank = "";
                  }
               }
               i++;
            }
            else if(command.equals("-address") && args.length > (i + 1))
            {
               host = args[i+1];
               i++;
            }
            else if(command.equals("-port") && args.length > (i + 1))
            {
               port = Integer.valueOf(args[i+1]);
               i++;
            }
            else
            {
               System.out.println("Le programme possède quatres commandes (dans n'importe quel ordre) : ");
               System.out.println("-victim  (obligatoire)      : chemin vers le fichier contenant les groupes de victimes");
               System.out.println("-pranks  (obligatoire)      : chemin vers le fichier contenant les blagues");
               System.out.println("-address (defaut=localhost) : adresse IP ou URL du serveur SMTP");
               System.out.println("-port    (defaut=2525)      : port du serveur SMTP");
               return;
            }
         }
         catch (Exception e)
         {
            System.out.println("Le fichier " + args[i] + " n'existe pas ou " +
                  "ne peut pas être lu. \r\nErreur --> " + e.toString());
            return;
         }
      }

      connect();

      try
      {
         for (Group group : groups)
         {
            for (String sender : group.getSenders())
            {
               sendMail(
                     sender,
                     "Let's LOL",
                     (String)(pranks.toArray()[Math.abs(rand.nextInt()%pranks.size())]),
                     group.getReceivers()
               );
            }
         }
      }
      catch (Exception e)
      {
         System.out.println("Le fichier des groupes est mal formaté : " + e.toString());
      }

      try
      {
         System.in.read();
      }
      catch (IOException e)
      {
         System.out.println("Il y a eu un souci avec votre clavier : "
                 + e.toString());
      }
   }

   private static void connect()
   {
      try
      {
         socket = new Socket(host, port);
      }
      catch (IOException e)
      {
         System.out.println("Il y a eu un souci à la connexion : " + e.toString());
         return;
      }

      try
      {
         writer = new SmtpWriter(new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()))));
         reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
      }
      catch (IOException e)
      {
         System.out.println("Il y a eu un souci à la création du reader et writer : "
                 + e.toString());
         return;
      }
   }


   private static void sendMail(String emailFrom, String subject, String message, String... emailsTo)
   {
      //Récupération de la ligne de bienvenue
      easyRead();

      //Envoi de la commande EHLO
      sendAndFlush("EHLO " + name);

      //Récupération de la réponse (infos sur les capacités)
      easyRead();


      //Envoi de la source du message
      sendAndFlush("MAIL FROM: " + emailFrom);

      //Récupération de la réponse (email from ok)
      easyRead();

      for (String emailTo : emailsTo)
      {
         //Envoi de chaque destinataire du message
         sendAndFlush("RCPT TO: " + emailTo);
         //Récupération de la réponse (email to ok)
         easyRead();
      }

      //Demande d'envoie du contenu du message
      sendAndFlush("DATA");
      //Récupération de la réponse (start data)
      easyRead();

      //Envoi des données
      List<String> msgBody = makeHeader(emailFrom, subject, emailsTo);
      msgBody.add(message);
      sendAndFlush(msgBody.toArray(new String[0]));

      //Envoi de la commande de fin de données
      sendAndFlush(END_OF_DATA);
   }

   private static List<String> makeHeader(String emailFrom, String subject,String... emailsTo)
   {
      List<String> header = new ArrayList<String>();
      header.add("From: " + emailFrom);
      for (int i = 0; i < emailsTo.length; ++i)
      {
         header.add((i == 0) ? "To: " : "Cc: " + emailsTo[i]);
      }
      header.add("Subject: " + subject);
      header.add("");
      return header;
   }

   private static void easyRead()
   {
      try
      {
         String lastLine;
         do
         {
            lastLine = reader.readLine();
            System.out.println(SERVER_P + lastLine);
         }
         while (lastLine.length() > 3 && lastLine.charAt(3) != ' ');
      }
      catch (IOException e)
      {
         System.out.println("Erreur lors de l'écoute du serveur : " + e.toString());
      }
   }

   private static void sendAndFlush(String... lines)
   {
      for (String line : lines)
      {
         System.out.println(CLIENT_P + line);
         writer.println(line);

      }
      writer.flush();
   }
}
