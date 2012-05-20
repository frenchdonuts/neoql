package net.ericaro.neoql;

import java.util.Random;

public class RandName {

	static String[] names = new String[] { "adanedhel", "adurant", "aeglos", "aegnor", "aerandir", "aerin", "agarwaen", "aglarond", "aglon", "ainur", "aldaron", "alfirin", "almandil", "almaren", "aman", "amanyar", "amarth", "amlach", "amon", "amras", "amrod", "amroth", "anacalagon", "anach", "andor", "andram", "androth", "anduin", "anfauglir", "anfauglith", "angainor", "angband", "anglachel", "angmar", "angrim", "angrod", "aragorn", "aranion", "aratar", "arathorn", "arda", "argonath", "arien", "armenelos", "arnoediad", "arnor", "arvernien", "arwen", "atani", "athelas", "avari", "avathar", "azanulbizar", "azog", "baggins", "balar", "balchoth", "balin", "balrog", "banakil", "bandobras", "baragund", "barahir", "barding", "belain", "belegaer", "belegost", "beleriand", "beorn", "beorning", "beren", "bifur", "bilbo", "bofur", "bolg", "bombur", "boromir", "brandir", "bregolas", "bregor", "brethil", "cair", "calacirya", "calaquendi", "calenardhon", "camlost", "caranthir", "caras", "carcharoth", "carnil", "celeborn", "celebrant", "celebrian", "celebrimbor", "celebrindal", "celegorm", "celon", "cerin", "cirdan", "cirith", "cirth", "ciryon", "culumalda", "curufin", "curunir", "daeron", "dagnir", "dagorlad", "dairuin", "denethor", "dimbar", "dori", "doriath", "dorlas", "dorwinion", "draugluin", "dumbledors", "dunharrow", "dunlending", "durin", "dwalin", "dwimmerlaik", "easterling", "edain", "edhil", "edoras", "eglath", "eilinel", "ekkaia", "elanor", "elbereth", "eldamar", "eldarin", "elendil", "elendili", "elendur", "elenna", "elladan", "elrohir", "elrond", "elros", "elwing", "emeldir", "engwar", "entwood", "eorl", "erebor", "eregion", "eriador", "esgaroth", "estolad", "evermind", "falas", "falathrim", "fallohides", "falmari", "fangorn", "faramir", "fastitocalon", "felagund", "finarfin", "findulias", "fingolfin", "fingon", "finrod", "forgoil", "formenos", "fornost", "forochel", "forodwaith", "frodo", "frogmorton", "galadhrim", "galadriel", "galdor", "galenas", "galvorn", "gamgee", "gandalf", "gaurhoth", "gelion", "gelmir", "gilgalad", "gil-galad", "gimli", "gladden", "glamhoth", "glaurung", "glorfindel", "goblin", "goldberry", "golfimbul", "gollum", "golodhrim", "gondolin", "gondor", "gonnhirrim", "gorbag", "gorcrows", "gorgoroth", "gothmog", "grond", "guilin", "guldur", "gundor", "gurthang", "gwaihir", "gwaith", "gwindor", "hador", "haladin", "haldad", "haldan", "haldir", "haleth", "half-orc", "halmir", "handir", "harad", "haradrim", "haradwaith", "hareth", "harfoot", "hathaldir", "helcar", "helevorn", "helluin", "helmingas", "herumor", "hildor", "himlad", "himring", "hirilorn", "hithaeglir", "hithlum", "hobbit", "hobbiton", "hobgoblin", "holbytlan", "hollin", "hollowbold", "hornburg", "huan", "hummerhorn", "hunthor", "huor", "huorn", "hyarmen", "hyarmentir", "idril", "ilmarin", "ilmen", "imadris", "imlach", "imladris", "imrahil", "indis", "isen", "isengard", "isengrim", "isil", "isildur", "istari", "ivrin", "kelvar", "kingsfoil", "kirinki", "kraken", "ladros", "laiquendi", "lalaith", "lameth", "lammoth", "landroval", "lanthir", "laurelin", "leaflock", "legolas", "lembas", "lhaw", "linaewen", "lindar", "lindon", "lissuin", "lorgan", "lorien", "lossoth", "lothlann", "mablung", "maedhros", "maeglin", "maglor", "maia", "maiar", "malach", "mallorn", "mallos", "mandos", "mardil", "marish", "mathan", "mearas", "meduseld", "melian", "melkor", "menegroth", "meneltarma", "mereth", "meriadoc", "mewlip", "minas", "minastir", "mindeb", "mirkwood", "mithrandir", "mordor", "morgoth", "morgul", "moria", "moriquendi", "mormegil", "morwen", "nahar", "nandor", "nargothrond", "narog", "narsil", "narsilion", "narya", "naugrim", "neekerbreeker", "neldor", "neldoreth", "nenuial", "nerdanel", "nessa", "nessamelda", "nevrast", "nibin", "nienna", "nienor", "nimbrethil", "nimrodel", "niphredil", "nirnaeth", "noegyth", "nogrod", "noldor", "nurn", "oakenshield", "oatbarton", "oliphaunt", "olog-hai", "olvar", "onodrim", "orc", "ori", "orocarni", "orodreth", "orodruin", "orthanc", "osgiliath", "ossirand", "pelargir", "pelennor", "peregrin", "periannath", "pippin", "quendi", "quickbeam", "radagast", "radgbug", "rauko", "rauros", "ravenhill", "rhovanion", "ringwraith", "rivendell", "rohan", "rohirrim", "rothinzil", "samwise", "saruman", "sauron", "scatha", "sereg", "seregon", "shadowfax", "shagrat", "shelob", "silmaril", "silvan", "sindar", "sindarin", "sirion", "skinbark", "smaug", "smial", "snaga", "snowmane", "southron", "stoor", "swerting", "talath", "taniquetil", "tareldar", "tark", "tasarion", "tathar", "tauron", "telcontari", "teleri", "thain", "thalion", "thangorodrim", "thaur", "thingol", "thorin", "thoron", "thorondor", "thrush", "thuringwethil", "tilion", "tirion", "tirith", "tobold", "tol", "torog", "treebeard", "trollshaw", "tuckborough", "tulkas", "tuor", "turambar", "turgon", "uial", "uilos", "uinen", "uldor", "ulfang", "ulfast", "ulmo", "ulwarth", "umbar", "ungoliant", "urthel", "uruk", "uruk-hai", "utumno", "valaquenta", "valar", "valaraukar", "valimar", "valinor", "valvar", "vanyar", "varda", "vardarianna", "variag", "wainrider", "wandlimb", "wereworm", "westmansweed", "wethrin", "wilwarin", "withywindle", "wose", "yavanna", "yrch", "zirak-zigil", };
	static Random rand = new Random();
	public static String next() {
		return names[rand.nextInt(names.length)];
	}
}
