package com.example.curtis.swt_quiz_app;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.query.ResultSetFactory;
import com.hp.hpl.jena.query.ResultSetFormatter;

import java.io.ByteArrayOutputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;


/**
 * Created by curtis on 24/11/15.
 */
public class SparqlHelper {
    //private static final String KEY_ID = "id";
    private static final String KEY_QUES = "question";
    private static final String KEY_OPTA= "opta"; //option a
    private static final String KEY_OPTB= "optb"; //option b
    private static final String KEY_OPTC= "optc"; //option c
    private static final String KEY_OPTD = "optd"; //option d
    private static final String KEY_ANSWER = "answer"; //correct option

    public Question getNewQuestion(int diff,  List<String> b, List<String> bS, List<String> bM, List<String> iB, List<String> aB, List<String> m,  List<String> mS, List<String> iM, List<String> aM,  List<String> cs) {
        //probability for musicalWork (album/song) changes
        final int musicalWorkMax = 4; //randomInt: 0=change, all other options until max=no change
        final int artistMax = 1;
        int difficulty = diff;
        List<String> bands = b;
        List<String> bandsSong = bS;
        List<String> bandsMembers = bM;
        List<String> inactiveBands = iB;
        List<String> allBands = aB;
        List<String> musicians = m;
        List<String> musiciansSong = mS;
        List<String> inactiveMusicians = iM;
        List<String> allMusicians = aM;
        List<String> countriesAndStates = cs;



        int qType = 0; //question type
        int listNum = 0; //band/artist identifier, 0:band, 1:musicalArtist
        int resultCounter = 0; //counts the number of result of the query

        String queryString = "";
        String artist = "";
        String band = "";
        String startYear = "";
        String endYear = "";
        String hometown = "";
        String albumname = "";
        String songname = "";

        RandomInt randomWork = new RandomInt();
        int album;

        Question q = new Question();
        QueryString qs = new QueryString();

        //random question type
        RandomInt randomIntQ = new RandomInt();
        qType = randomIntQ.getRandInt(0, 5); //randInt(min, max)

        //random list: bands or musicians
        RandomInt randomIntL = new RandomInt();
        switch (qType) {
            case 0: //0:career start of artist/band
                listNum = randomIntL.getRandInt(0, 1);
                if (listNum == 0) { //band
                    queryString = qs.getQueryString(qType, listNum, bands);
                } else { //musician
                    queryString = qs.getQueryString(qType, listNum, musicians);
                }
                break;
            case 1: //1:career end of artist/band
                listNum = randomIntL.getRandInt(0, 1);
                if (listNum == 0) { //band
                    queryString = qs.getQueryString(qType, listNum, inactiveBands);
                } else { //musician
                    queryString = qs.getQueryString(qType, listNum, inactiveMusicians);
                }
                break;
            case 2: //2:hometown of artist/band
                listNum = randomIntL.getRandInt(0, 1);
                if (listNum == 0) { //band
                    queryString = qs.getQueryString(qType, listNum, allBands);
                } else { //musician
                    queryString = qs.getQueryString(qType, listNum, allMusicians);
                }
                break;
            case 3: //3:which album is from the following artist/band
                listNum = randomIntL.getRandInt(0, 1);
                if (listNum == 0) { //band
                    queryString = qs.getQueryString(qType, listNum, allBands);
                } else { //musician
                    queryString = qs.getQueryString(qType, listNum, allMusicians);
                }
                break;
            case 4: //4: which song is from the following artist/band
                listNum = randomIntL.getRandInt(0, 1);
                if (listNum == 0) { //band
                    queryString = qs.getQueryString(qType, listNum, bandsSong);
                } else { //musician
                    queryString = qs.getQueryString(qType, listNum, musiciansSong);
                }
                break;
            case 5: //5: which artist is a member of the following band
                listNum = 0; //only for bands
                queryString = qs.getQueryString(qType, listNum, bandsMembers);
                break;
        }



        Query query = QueryFactory.create(queryString);
        String service = "http://dbpedia.org/sparql";
        QueryExecution qexec = QueryExecutionFactory.sparqlService(service, query);

        try {

            ResultSet results = qexec.execSelect();
            ResultSet output = ResultSetFactory.copyResults(qexec.execSelect());
            System.out.println(ResultSetFormatter.asText(output));
            //ResultSetFormatter.out(System.out, output);

            List<String> seenArtist = new ArrayList<String>();
            List<String> seenBands = new ArrayList<String>();
            List<String> answerBandMembers = new ArrayList<String>();
            String currentArtist = "";
            String currentBand = "";
            String lastArtist = "";
            String lastBand = "";
            String lastAnswer ="";

            String rightAnswer = "";
            String wrongAnswer1 = "";
            String wrongAnswer2 = "";
            String wrongAnswer3 = "";

            nextResultLoop:
            while (results.hasNext()) {
                QuerySolution sol = results.next();
                ResultVariables resultVariables = new ResultVariables();
                List<String> wrongAnswers = new ArrayList<String>();

                switch (qType) {

                    case 0: //0:Career start of artist/band
                        //get artist
                        artist = resolveUTF8(resultVariables.getArtist(sol));

                        //get dboStartYear
                        startYear = resultVariables.getStartYear(sol);

                        q.setQUESTION("When did the musical career of \n" + artist + " start?");
                        q.setANSWER(startYear);

                        //ger right answer
                        rightAnswer = startYear;

                        wrongAnswers = getWrongAnswerOptionsYear(rightAnswer, difficulty);
                        wrongAnswer1 = wrongAnswers.get(0);
                        wrongAnswer2 = wrongAnswers.get(1);
                        wrongAnswer3 = wrongAnswers.get(2);

                        break;
                    case 1: //0:Career end of artist/band
                        //get artist
                        artist = resolveUTF8(resultVariables.getArtist(sol));

                        //get dboEndYear (if available)
                        endYear = resultVariables.getEndYear(sol);

                        q.setQUESTION("When did the musical career of \n" + artist + " end?");
                        q.setANSWER(endYear);

                        //ger right answer
                        rightAnswer = endYear;

                        wrongAnswers = getWrongAnswerOptionsYear(rightAnswer, difficulty);
                        wrongAnswer1 = wrongAnswers.get(0);
                        wrongAnswer2 = wrongAnswers.get(1);
                        wrongAnswer3 = wrongAnswers.get(2);
                        break;
                    case 2: //2:hometown of artist/band
                        switch (resultCounter) {
                            case 0:
                                artist = resolveUTF8(resultVariables.getArtist(sol));
                                hometown = resolveUTF8(resultVariables.getHometown(sol));
//check against same values!
//add placeholder cities!
                                //check for next artist without increasing result counter
                               if (artist.equals(lastArtist) || lastArtist == "") {
                                   if (!countriesAndStates.contains(hometown)) {
                                       q.setQUESTION("Where is " + artist + " from?");
                                       q.setANSWER(hometown);
                                       rightAnswer = hometown;
                                       seenArtist.add(artist);
                                       lastArtist = "";
                                       resultCounter++;
                                   } else {
                                       lastArtist = artist;
                                       lastAnswer = hometown;
                                   }
                                //no valid values in hometown attribute
                               } else {
                                   q.setQUESTION("Where is " + lastArtist + " from?");
                                   q.setANSWER(lastAnswer);
                                   rightAnswer = lastAnswer;
                                   seenArtist.add(lastArtist);
                                   resultCounter++;
                                   //do case 1 here already as new artist is queried and old artist has no valid attribute
                                   currentArtist = resolveUTF8(resultVariables.getArtist(sol));
                                   if (!seenArtist.contains(currentArtist)){

                                       wrongAnswer1 = resolveUTF8(resultVariables.getHometown(sol));
                                       if (!countriesAndStates.contains(wrongAnswer1)) {
                                           seenArtist.add(currentArtist);
                                           lastArtist = "";
                                           resultCounter++;
                                       } else {
                                           lastArtist = currentArtist;
                                           lastAnswer = hometown;
                                       }
                                   }
                               }
                                break;
                            case 1:
                                currentArtist = resolveUTF8(resultVariables.getArtist(sol));
                                if (currentArtist.equals(lastArtist) || lastArtist =="") {
                                    if (!seenArtist.contains(currentArtist)){
                                        wrongAnswer1 = resolveUTF8(resultVariables.getHometown(sol));
                                        if (!countriesAndStates.contains(wrongAnswer1)) {
                                            seenArtist.add(currentArtist);
                                            lastArtist = "";
                                            resultCounter++;
                                        } else {
                                            lastArtist = currentArtist;
                                            lastAnswer = hometown;
                                        }
                                    }
                                } else {
                                    wrongAnswer1 = lastAnswer;
                                    seenArtist.add(lastArtist);
                                    resultCounter++;
                                    //do case 2 already as new artist is queried and old artist has no valid attribute
                                    currentArtist = resolveUTF8(resultVariables.getArtist(sol));
                                    if (!seenArtist.contains(currentArtist)){
                                        wrongAnswer2 = resolveUTF8(resultVariables.getHometown(sol));
                                        if (!countriesAndStates.contains(wrongAnswer2)) {
                                            seenArtist.add(currentArtist);
                                            lastArtist = "";
                                            resultCounter++;
                                        } else {
                                            lastArtist = currentArtist;
                                            lastAnswer = hometown;
                                        }
                                    }
                                }
                                break;
                            case 2:
                                currentArtist = resolveUTF8(resultVariables.getArtist(sol));
                                if (currentArtist.equals(lastArtist) || lastArtist == "") {
                                    if (!seenArtist.contains(currentArtist)) {
                                        wrongAnswer2 = resolveUTF8(resultVariables.getHometown(sol));
                                        if (!countriesAndStates.contains(wrongAnswer2)) {
                                            seenArtist.add(currentArtist);
                                            lastArtist = "";
                                            resultCounter++;
                                        } else {
                                            lastArtist = currentArtist;
                                            lastAnswer = hometown;
                                        }
                                    }
                                } else {
                                    wrongAnswer1 = lastAnswer;
                                    seenArtist.add(lastArtist);
                                    resultCounter++;
                                    //do case 2 already as new artist is queried and old artist has no valid attribute
                                    currentArtist = resolveUTF8(resultVariables.getArtist(sol));
                                    if (!seenArtist.contains(currentArtist)){
                                        wrongAnswer3 = resolveUTF8(resultVariables.getHometown(sol));
                                        if (!countriesAndStates.contains(wrongAnswer3)) {
                                            seenArtist.add(currentArtist);
                                            lastArtist = "";
                                            resultCounter++;
                                        } else {
                                            lastArtist = currentArtist;
                                            lastAnswer = hometown;
                                        }
                                    }

                                }
                                break;
                            case 3:
                                    currentArtist = resolveUTF8(resultVariables.getArtist(sol));
                                    if (!seenArtist.contains(currentArtist)) {
                                        wrongAnswer3 = resolveUTF8(resultVariables.getHometown(sol));
                                        if (!countriesAndStates.contains(wrongAnswer3) || results.hasNext() == false) {
                                            seenArtist.add(currentArtist);
                                            lastArtist = "";
                                            resultCounter++;
                                            break nextResultLoop;
                                        }
                                    }
                                break;
                            default:
                                currentArtist = resolveUTF8(resultVariables.getArtist(sol));
                                if (!seenArtist.contains(currentArtist)){
                                    wrongAnswer3 = resolveUTF8(resultVariables.getHometown(sol));
                                        seenArtist.add(currentArtist);
                                }
                                break;
                        }
                        continue nextResultLoop;
                    case 3: //3:which album is from the following artist/band
                        switch (resultCounter) {
                            case 0:
                                artist = resolveUTF8(resultVariables.getArtist(sol));
                                albumname = resolveUTF8(resultVariables.getAlbumname(sol));
                                q.setQUESTION("Which album was released by \n" + artist + "?");
                                q.setANSWER(albumname);
                                rightAnswer = albumname;
                                seenArtist.add(artist);
                                lastArtist = artist;
                                resultCounter++;
                                break;
                            case 1:
                                //check if next entry of result is from same artist of case 0
                                currentArtist = resolveUTF8(resultVariables.getArtist(sol));
                                albumname = resolveUTF8(resultVariables.getAlbumname(sol));
                                if (currentArtist.equals(lastArtist)) {
                                    //if entry is also from same artist
                                    //check albumname against artist name, live, greatest hits, best of
                                    album = checkAlbumname(albumname, currentArtist, musicalWorkMax);
                                    //change albumname if album == 0
                                    if (album == 0) {
                                        q.setANSWER(albumname);
                                        rightAnswer = albumname;
                                    }
                                } else{
                                    lastArtist = currentArtist;
                                }
                                //current artist is new artist
                                if (!seenArtist.contains(currentArtist)){
                                    wrongAnswer1 = resolveUTF8(resultVariables.getAlbumname(sol));
                                    seenArtist.add(currentArtist);
                                    lastArtist = currentArtist;
                                    resultCounter++;
                                }
                                break;
                            case 2:
                                //check if next entry of result is from the same artist of case 1
                                currentArtist = resolveUTF8(resultVariables.getArtist(sol));
                                albumname = resolveUTF8(resultVariables.getAlbumname(sol));
                                if (currentArtist.equals(lastArtist)) {
                                    album = checkAlbumname(albumname, currentArtist, musicalWorkMax);
                                    if (album == 0) {
                                        wrongAnswer1 = resolveUTF8(resultVariables.getAlbumname(sol));
                                    }
                                } else {
                                    lastArtist = currentArtist;
                                }
                                //current artist is new artist
                                if (!seenArtist.contains(currentArtist)){
                                    wrongAnswer2 = resolveUTF8(resultVariables.getAlbumname(sol));
                                    seenArtist.add(currentArtist);
                                    lastArtist = currentArtist;
                                    resultCounter++;
                                }
                                break;
                            case 3:
                                //check if next entry of result is from the same artist of case 2
                                currentArtist = resolveUTF8(resultVariables.getArtist(sol));
                                albumname = resolveUTF8(resultVariables.getAlbumname(sol));
                                if (currentArtist.equals(lastArtist)) {
                                    album = checkAlbumname(albumname, currentArtist, musicalWorkMax);
                                    if (album == 0) {
                                        wrongAnswer2 = resolveUTF8(resultVariables.getAlbumname(sol));
                                    }
                                } else {
                                    lastArtist = currentArtist;
                                }
                                if (!seenArtist.contains(currentArtist)){
                                    wrongAnswer3 = resolveUTF8(resultVariables.getAlbumname(sol));
                                    seenArtist.add(currentArtist);
                                    lastArtist = currentArtist;
                                    resultCounter++;
                                }
                                break;
                            default:
                                //check if next entry of result is from the same artist of case 3
                                currentArtist = resolveUTF8(resultVariables.getArtist(sol));
                                albumname = resolveUTF8(resultVariables.getAlbumname(sol));
                                if (currentArtist.equals(lastArtist)) {
//                                    album = randomWork.getRandInt(0, musicalWorkMax);
                                    //probability that the album name is changed
                                    album = checkAlbumname(albumname, currentArtist, musicalWorkMax);
                                    if (album == 0) {
                                        wrongAnswer3 = resolveUTF8(resultVariables.getAlbumname(sol));
                                    }
                                }
                                break;
                        }
                        continue nextResultLoop;
                    case 4: //3:which song is from the following artist/band
                        switch (resultCounter) {
                            case 0:
                                artist = resolveUTF8(resultVariables.getArtist(sol));
                                songname = resolveUTF8(resultVariables.getSongname(sol));
                                q.setQUESTION("Which song was released by \n" + artist + "?");
                                q.setANSWER(songname);
                                rightAnswer = songname;
                                seenArtist.add(artist);
                                lastArtist = artist;
                                resultCounter++;
                                break;
                            case 1:
                                //check if next entry of result is from same artist of case 0
                                currentArtist = resolveUTF8(resultVariables.getArtist(sol));
                                if (currentArtist.equals(lastArtist)) {
                                    //if entry is also from same artist
                                    int song = randomWork.getRandInt(0,musicalWorkMax);
                                    //probability that the song name is changed
                                    if (song == 0) {
                                        songname = resolveUTF8(resultVariables.getSongname(sol));
                                        q.setANSWER(songname);
                                        rightAnswer = songname;
                                    }
                                } else{
                                    lastArtist = currentArtist;
                                }
                                //current artist is new artist
                                if (!seenArtist.contains(currentArtist)){
                                    wrongAnswer1 = resolveUTF8(resultVariables.getSongname(sol));
                                    seenArtist.add(currentArtist);
                                    resultCounter++;
                                }
                                break;
                            case 2:
                                //check if next entry of result is from same artist of case 0
                                currentArtist = resolveUTF8(resultVariables.getArtist(sol));
                                if (currentArtist.equals(lastArtist)) {
                                    //if entry is also from same artist
                                    int song = randomWork.getRandInt(0,musicalWorkMax);
                                    //probability that the song name is changed
                                    if (song == 0) {
                                        wrongAnswer1 = resolveUTF8(resultVariables.getSongname(sol));
                                    }
                                } else{
                                    lastArtist = currentArtist;
                                }
                                //current artist is new artist
                                if (!seenArtist.contains(currentArtist)){
                                    wrongAnswer2 = resolveUTF8(resultVariables.getSongname(sol));
                                    seenArtist.add(currentArtist);
                                    resultCounter++;
                                }
                                break;
                            case 3:
                                //check if next entry of result is from same artist of case 0
                                currentArtist = resolveUTF8(resultVariables.getArtist(sol));
                                if (currentArtist.equals(lastArtist)) {
                                    //if entry is also from same artist
                                    int song = randomWork.getRandInt(0,musicalWorkMax);
                                    //probability that the song name is changed
                                    if (song == 0) {
                                        wrongAnswer2 = resolveUTF8(resultVariables.getSongname(sol));
                                    }
                                } else{
                                    lastArtist = currentArtist;
                                }
                                //current artist is new artist
                                if (!seenArtist.contains(currentArtist)){
                                    wrongAnswer3 = resolveUTF8(resultVariables.getSongname(sol));
                                    seenArtist.add(currentArtist);
                                    resultCounter++;
                                }
                                break;
                            default:
                                //check if next entry of result is from the same artist of case 3
                                currentArtist = resolveUTF8(resultVariables.getArtist(sol));
                                if (currentArtist.equals(lastArtist)) {
                                    int song = randomWork.getRandInt(0, musicalWorkMax);
                                    //probability that the song name is changed
                                    if (song == 0) {
                                        wrongAnswer3 = resolveUTF8(resultVariables.getSongname(sol));
                                    }
                                }
                                break;
                        }
                        continue nextResultLoop;
                    case 5:
                        switch (resultCounter) {
                            case 0:
                                artist = resolveUTF8(resultVariables.getArtist(sol));
                                band = resolveUTF8(resultVariables.getBand(sol));
                                q.setQUESTION("Who is a member of the band \n" + band + "?");
                                q.setANSWER(artist);
                                rightAnswer = artist;
                                seenBands.add(band);
                                answerBandMembers.add(artist);
                                lastBand = band;
                                wrongAnswer1 = "Stevie Wonder";
                                wrongAnswer2 = "James Brown";
                                wrongAnswer3 = "Ray Charles";
                                resultCounter++;
                                break;
                            case 1:
                                //check if next entry of result is from same band of case 0
                                currentBand = resolveUTF8(resultVariables.getBand(sol));
                                currentArtist = resolveUTF8(resultVariables.getArtist(sol));
                                if (currentBand.equals(lastBand)) {
                                    answerBandMembers.add(currentArtist);
                                    //if entry is also from same artist
                                    int randomArtist = randomWork.getRandInt(0,artistMax);
                                    //probability that the song name is changed
                                    if (randomArtist == 0) {
                                        artist = currentArtist;
                                        q.setANSWER(artist);
                                        rightAnswer = artist;
                                    }
                                } else{
                                    lastBand = currentBand;
                                }
                                //current band is new band
                                if (!seenBands.contains(currentBand)){
                                    if(!answerBandMembers.contains(currentArtist)) {
                                        wrongAnswer1 = currentArtist;
                                        seenBands.add(currentBand);
                                        resultCounter++;
                                    }
                                }
                                break;
                            case 2:
                                //check if next entry of result is from same band of case 1
                                currentBand = resolveUTF8(resultVariables.getBand(sol));
                                currentArtist = resolveUTF8(resultVariables.getArtist(sol));
                                if (currentBand.equals(lastBand)) {
                                    if(!answerBandMembers.contains(currentArtist)) {
                                        //if entry is also from same artist
                                        int randomArtist = randomWork.getRandInt(0,artistMax);
                                        //probability that the song name is changed
                                        if (randomArtist == 0) {
                                            wrongAnswer1 = currentArtist;
                                        }
                                    }
                                } else{
                                    lastBand = currentBand;
                                }
                                //current band is new band
                                if (!seenBands.contains(currentBand)){
                                    if( !answerBandMembers.contains(currentArtist)) {
                                        wrongAnswer2 = currentArtist;
                                        seenBands.add(currentBand);
                                        resultCounter++;
                                    }
                                }
                                break;
                            case 3:
                                //check if next entry of result is from same band of case 2
                                currentBand = resolveUTF8(resultVariables.getBand(sol));
                                currentArtist = resolveUTF8(resultVariables.getArtist(sol));
                                if (currentBand.equals(lastBand)) {
                                    if(!answerBandMembers.contains(currentArtist)) {
                                        //if entry is also from same artist
                                        int randomArtist = randomWork.getRandInt(0,artistMax);
                                        //probability that the song name is changed
                                        if (randomArtist == 0) {
                                            wrongAnswer1 = currentArtist;
                                        }
                                    }
                                } else{
                                    lastBand = currentBand;
                                }
                                //current band is new band
                                if (!seenBands.contains(currentBand)){
                                    if( !answerBandMembers.contains(currentArtist)) {
                                        wrongAnswer3 = currentArtist;
                                        seenBands.add(currentBand);
                                        resultCounter++;
                                    }
                                }
                                break;
                            default:
                                //check if next entry of result is from the same band of case 4
                                currentBand = resolveUTF8(resultVariables.getBand(sol));
                                currentArtist = resolveUTF8(resultVariables.getArtist(sol));
                                if (currentBand.equals(lastBand)) {
                                    if(!answerBandMembers.contains(currentArtist)) {
                                        int randomArtist = randomWork.getRandInt(0, artistMax);
                                        //probability that the song name is changed
                                        if (randomArtist == 0) {
                                            wrongAnswer3 = currentArtist;
                                        }
                                    }
                                }
                                break;
                        }

                        continue nextResultLoop;

                }
            }




            RandomInt randomAnswer = new RandomInt();
            int answerButton = randomAnswer.getRandInt(1, 4);
            
            switch (answerButton) {
                case 1:
                    q.setOPTA(rightAnswer);
                    q.setOPTB(wrongAnswer1);
                    q.setOPTC(wrongAnswer2);
                    q.setOPTD(wrongAnswer3);
                    break;
                case 2:
                    q.setOPTA(wrongAnswer1);
                    q.setOPTB(rightAnswer);
                    q.setOPTC(wrongAnswer2);
                    q.setOPTD(wrongAnswer3);
                    break;
                case 3:
                    q.setOPTA(wrongAnswer1);
                    q.setOPTB(wrongAnswer2);
                    q.setOPTC(rightAnswer);
                    q.setOPTD(wrongAnswer3);
                    break;
                case 4:
                    q.setOPTA(wrongAnswer1);
                    q.setOPTB(wrongAnswer2);
                    q.setOPTC(wrongAnswer3);
                    q.setOPTD(rightAnswer);
                    break;
            }


        } catch (Exception e) {
            e.printStackTrace();
            //no dbpedia connection
            //"The web-site you are currently trying to access is under maintenance at this time.""
        }
        qexec.close();

        return q;
    }

    /**
     * resolve UTF-8 hex. character sequences
     * looks for "%" (as contained in DBpedia URIs) and resolves the sequence into a character
     * @param resolveString the string to resolve
     * @return
     */
    private String resolveUTF8(String resolveString) {
        String result = resolveString;
        boolean containsHex;

        if(!result.contains("%")) {
            containsHex = false;
            return result;
        } else {
            containsHex = true;
            //full hex string to resolve (including %)
            String resolveHex;
            //hex sequence to resolve (without %)
            String resolveHexSeq;
            //resolved hex sequence
            String resolvedHex;
            //start of hex string to resolve
            int indexHexB;
            //end of hex string to resolve
            int indexHexE;

            while(containsHex) {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                //get beginning of hex substring
                indexHexB = resolveString.indexOf("%");
                //get end of hex substring (+3 as "%" is included)
                indexHexE = indexHexB + 3;
                //get full hex substring that should be replaced
                resolveHex = resolveString.substring(indexHexB, indexHexE);
                //get substring with hex sequence to be resolved
                resolveHexSeq = resolveString.substring(indexHexB+1, indexHexE);
                //resolve hex sequence
                int byteVal = Integer.parseInt(resolveHexSeq, 16);
                baos.write(byteVal);
                //get resolved symbol
                resolvedHex = new String(baos.toByteArray(), Charset.forName("UTF-8"));

                //replace full hex substring (including "%") with resolved symbol
                result = result.replace(resolveHex, resolvedHex);
                //check if more hex substrings are contained
                if(!result.contains("%")) {
                    containsHex = false;
                }
            }
            return result;
        }
    }

    /**
     * check albumname against "live", "greatest hits", "best of", and currentArtist name
     * @param an albumname
     * @param ca currentArtist
     * @param mwm musicalWorkMax
     * @return
     */
    private Integer checkAlbumname(String an, String ca, int mwm) {
        int album;
        RandomInt randomAlbum = new RandomInt();

        String albumname = an;
        String currentArtist = ca;
        int musicalWorkMax = mwm;

        //check albumname against artist name, live, greatest hits, best of
        if(albumname.toLowerCase().contains("live in") ||
                albumname.toLowerCase().contains("live at") ||
                albumname.toLowerCase().contains("live from") ||
                albumname.toLowerCase().contains("greatest hits") ||
                albumname.toLowerCase().contains("best of")||
                albumname.toLowerCase().contains("singles") ||
                albumname.toLowerCase().contains("limited edition") ||
                albumname.toLowerCase().contains(currentArtist.toLowerCase()) ){
            //do not change album name, album != 0
            album = musicalWorkMax;
        } else {
            album = randomAlbum.getRandInt(0,musicalWorkMax);
            //probability that the album name is changed
        }

        return album;
    }


    private List<String> getWrongAnswerOptionsYear(String rA, int difficulty) {

        String rightAnswer = rA;

        //get current year
        Calendar calendar = Calendar.getInstance();
        int currentYear = calendar.get(Calendar.YEAR);

        //store the results
        List<String> result = new ArrayList<String>();

        boolean w1search = true;
        boolean w2search = true;
        boolean w3search = true;
        String wrongAnswer1 = "";
        String wrongAnswer2 = "";
        String wrongAnswer3 = "";
        int wrongAnswerInt1 = 0;
        int wrongAnswerInt2 = 0;
        int wrongAnswerInt3 = 0;

        //get first wrong answer
        while (w1search) {
            AnswerOptions w1 = new AnswerOptions();
            wrongAnswerInt1 = w1.getYearNumber(rightAnswer, difficulty);
            wrongAnswer1 = String.valueOf(wrongAnswerInt1);
            if (!wrongAnswer1.equals(rightAnswer) &&
                    wrongAnswerInt1 <= currentYear) {
                result.add(wrongAnswer1);
                w1search = false;
            }
        }

        //get second wrong answer
        while (w2search) {
            AnswerOptions w2 = new AnswerOptions();
            wrongAnswerInt2 = w2.getYearNumber(rightAnswer, difficulty);
            wrongAnswer2 = String.valueOf(wrongAnswerInt2);
            if (!wrongAnswer2.equals(rightAnswer) &&
                    !wrongAnswer2.equals(wrongAnswer1) &&
                    wrongAnswerInt2 <= currentYear) {
                result.add(wrongAnswer2);
                w2search = false;
            }
        }

        //get third wrong answer
        while (w3search) {
            AnswerOptions w3 = new AnswerOptions();
            wrongAnswerInt3 = w3.getYearNumber(rightAnswer, difficulty);
            wrongAnswer3 = String.valueOf(wrongAnswerInt3);
            if (!wrongAnswer3.equals(rightAnswer) &&
                    !wrongAnswer3.equals(wrongAnswer1) &&
                    !wrongAnswer3.equals(wrongAnswer2) &&
                    wrongAnswerInt3 <= currentYear) {
                result.add(wrongAnswer3);
                w3search = false;
            }
        }
        return result;
    }

}
