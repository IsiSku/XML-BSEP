Obrazlo�enje xml �ema.

Propis:
	Odradila sam neke izmene �to se tice strukture jer je na nekim mestima stajala sekvenca gde je trebalo da stoji izbor jer bi u suprotnom ispalo da mo�e� imati niz me�an od stavova i clanova npr.
	Sekvenca je strogo formatiran niz elemenata, nije skup, zato imamo choice. S obzirom da skoro svaki element od clana pa na dole mo�e imati ili niz podelemenata ili direktno tekst.

Amandman:
	Predlog_propisa sadr�i naziv propisa koj se menja (od onih koji su u proceduri).
	Jedan dokument mo�e sadr�ati vi�e amandmana na isti propis, zato je postavljeno na 1...inf
	Odredba je ni�ta drugo do selektor elementa koji se menja - ovaj amandman se odnosi na taj i taj deo propisa.
	Predlog_resenja sada ima tip u atributu da bi se znalo �ta se radi sa tim. Za dodavanje treba da se dogovorimo da li ce biti append na referencirani element.
		Pored toga ima izbor izmedju elemenata - to ce biti novi sadr�aj referenciranog elementa.
	Svaki amandman ima obrazlo�enje za�to je tu.