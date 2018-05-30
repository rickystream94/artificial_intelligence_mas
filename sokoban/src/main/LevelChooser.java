package main;

public class LevelChooser {

    private static final String rootPath = "../../../environment/levels/";
    private static final String ext = ".lvl";
    public static final String competitionPath = rootPath + "competition_levels/";

    // Example levels
    public static final String SAchoice = rootPath + "SAchoice" + ext;
    public static final String SAanagram = rootPath + "SAanagram" + ext;
    public static final String SApushing = rootPath + "SApushing" + ext;
    public static final String SAsimple1 = rootPath + "SAsimple1" + ext;
    public static final String SAsimple2 = rootPath + "SAsimple2" + ext;
    public static final String SAsoko1_12 = rootPath + "SAsoko1_12" + ext;
    public static final String SAbispebjergHospital = rootPath + "SAbispebjergHospital" + ext;
    public static final String SAsokobanLevel96 = rootPath + "SAsokobanLevel96" + ext;
    public static final String SAtest = rootPath + "SAtest" + ext;
    public static final String SAtowersOfSaigon5 = rootPath + "SAtowersOfSaigon5" + ext;
    public static final String MAsimple1 = rootPath + "MAsimple1" + ext;
    public static final String MAsimple2 = rootPath + "MAsimple2" + ext;
    public static final String MAsimple3 = rootPath + "MAsimple3" + ext;
    public static final String MAsimple4 = rootPath + "MAsimple4" + ext;
    public static final String MAsimple5 = rootPath + "MAsimple5" + ext;
    public static final String MAthomasAppartment = rootPath + "MAthomasAppartment" + ext;
    public static final String MAbispebjergHospital = rootPath + "MAbispebjergHospital" + ext;
    public static final String MAmultiagentSort = rootPath + "MAmultiagentSort" + ext;
    public static final String MApacman = rootPath + "MApacman" + ext;
    public static final String MAchallenge = rootPath + "MAchallenge" + ext;
    public static final String MAhelp = rootPath + "custom_levels/MAhelp" + ext;
    public static final String MAStuckByBox = rootPath + "custom_levels/MAStuckByBox" + ext;
    public static final String MAByteMe = rootPath + "competition_levels/MAByteMe" + ext;
    public static final String SAByteMe = rootPath + "competition_levels/SAByteMe" + ext;
    public static final String SAAlpha = rootPath + "example_levels/single_agent/medium/SAAlpha" + ext;
    public static final String SADCN = rootPath + "example_levels/single_agent/medium/SADCN" + ext;
    public static final String SAAteam = rootPath + "example_levels/single_agent/medium/SAAteam" + ext;
    public static final String SABullFight = rootPath + "example_levels/single_agent/medium/SABullFight" + ext;
    public static final String SAFOAM = rootPath + "example_levels/single_agent/medium/SAFOAM" + ext;
    public static final String SAholdkaeft = rootPath + "example_levels/single_agent/medium/SAholdkaeft" + ext;
    public static final String SAnull = rootPath + "example_levels/single_agent/medium/SAnull" + ext;
    public static final String SATeamNoOp = rootPath + "example_levels/single_agent/medium/SATeamNoOp" + ext;
    public static final String SAwallE = rootPath + "example_levels/single_agent/medium/SAwallE" + ext;
    public static final String SAbruteforce = rootPath + "example_levels/single_agent/easy/SAbruteforce" + ext;
    public static final String SACrunch = rootPath + "example_levels/single_agent/easy/SACrunch" + ext;
    public static final String SADeliRobot = rootPath + "example_levels/single_agent/easy/SADeliRobot" + ext;
    public static final String SAFirefly = rootPath + "example_levels/single_agent/easy/SAFirefly" + ext;
    public static final String SAgroup42 = rootPath + "example_levels/single_agent/easy/SAgroup42" + ext;
    public static final String SARyther = rootPath + "example_levels/single_agent/easy/SARyther" + ext;
    public static final String SAsampdoria = rootPath + "example_levels/single_agent/easy/SAsampdoria" + ext;

    // Competition levels
    public static final String SAAiAiCap = competitionPath + "SAAiAiCap" + ext;
    public static final String MAAlphaOne = competitionPath + "MAAlphaOne" + ext;
    public static final String MABeTrayEd = competitionPath + "MABeTrayEd" + ext;
    public static final String MADaVinci = competitionPath + "MADaVinci" + ext;
    public static final String MAKarlMarx = competitionPath + "MAKarlMarx" + ext;
    public static final String MAAIFather = competitionPath + "MAAIFather" + ext;
    public static final String MACybot = competitionPath + "MACybot" + ext;
    public static final String MANavy = competitionPath + "MANavy" + ext;
    public static final String MAJMAI = competitionPath + "MAJMAI" + ext;
    public static final String MAPushPush = competitionPath + "MAPushPush" + ext;
    public static final String MAZEROagent = competitionPath + "MAZEROagent" + ext;
    public static final String MAora = competitionPath + "MAora" + ext;
    public static final String MAAntsStar = competitionPath + "MAAntsStar" + ext;
    public static final String MALobot = competitionPath + "MALobot" + ext;
    public static final String SABeTrayEd = competitionPath + "SABeTrayEd" + ext;
    public static final String MAAiMasTers = competitionPath + "MAAiMasTers" + ext;
    public static final String SADaVinci = competitionPath + "SADaVinci" + ext;
    public static final String MAbongu = competitionPath + "MAbongu" + ext;
    public static final String SAAlphaOne = competitionPath + "SAAlphaOne" + ext;
    public static final String SAAIFather = competitionPath + "SAAIFather" + ext;
    public static final String SANikrima = competitionPath + "SANikrima" + ext;
    public static final String SAAiMasTers = competitionPath + "SAAiMasTers" + ext;
    public static final String MAdashen = competitionPath + "MAdashen" + ext;
    public static final String SAKaldi = competitionPath + "SAKaldi" + ext;
    public static final String SAdashen = competitionPath + "SAdashen" + ext;
    public static final String SAKarlMarx = competitionPath + "SAKarlMarx" + ext;
    public static final String SAEasyPeasy = competitionPath + "SAEasyPeasy" + ext;
    public static final String SANavy = competitionPath + "SANavy" + ext;
    public static final String SAPushPush = competitionPath + "SAPushPush" + ext;

    // Custom made levels
    public static final String SAPyramid = rootPath + "custom_levels/SAPyramid" + ext;
    public static final String SAPikachu = rootPath + "custom_levels/SAPikachu" + ext;
    public static final String MAHanoi = rootPath + "custom_levels/MAHanoi" + ext;
    public static final String SAmultibox = rootPath + "custom_levels/SAmultibox" + ext;
    public static final String MApriority = rootPath + "custom_levels/MApriority" + ext;
}
