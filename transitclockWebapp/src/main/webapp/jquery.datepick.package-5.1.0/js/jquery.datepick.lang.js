/*! http://keith-wood.name/datepick.html
   Datepicker localisations. */
/* http://keith-wood.name/datepick.html
   Afrikaans localisation for jQuery Datepicker.
   Written by Renier Pretorius and Ruediger Thiede. */
(function($) {
	'use strict';
	$.datepick.regionalOptions.af = {
		monthNames: ['Januarie','Februarie','Maart','April','Mei','Junie',
		'Julie','Augustus','September','Oktober','November','Desember'],
		monthNamesShort: ['Jan','Feb','Mrt','Apr','Mei','Jun',
		'Jul','Aug','Sep','Okt','Nov','Des'],
		dayNames: ['Sondag','Maandag','Dinsdag','Woensdag','Donderdag','Vrydag','Saterdag'],
		dayNamesShort: ['Son','Maan','Dins','Woens','Don','Vry','Sat'],
		dayNamesMin: ['So','Ma','Di','Wo','Do','Vr','Sa'],
		dateFormat: 'dd/mm/yyyy',
		firstDay: 1,
		renderer: $.datepick.defaultRenderer,
		prevText: 'Vorige',
		prevStatus: 'Vertoon vorige maand',
		prevJumpText: '&#x3c;&#x3c;',
		prevJumpStatus: 'Vertoon vorige jaar',
		nextText: 'Volgende',
		nextStatus: 'Vertoon volgende maand',
		nextJumpText: '&#x3e;&#x3e;',
		nextJumpStatus: 'Vertoon volgende jaar',
		currentText: 'Vandag',
		currentStatus: 'Vertoon huidige maand',
		todayText: 'Vandag',
		todayStatus: 'Vertoon huidige maand',
		clearText: 'Vee uit',
		clearStatus: 'Verwyder die huidige datum',
		closeText: 'Klaar',
		closeStatus: 'Sluit sonder verandering',
		yearStatus: 'Vertoon \'n ander jaar',
		monthStatus: 'Vertoon \'n ander maand',
		weekText: 'Wk',
		weekStatus: 'Week van die jaar',
		dayStatus: 'Kies DD, M d',
		defaultStatus: 'Kies \'n datum',
		isRTL: false
	};
	$.datepick.setDefaults($.datepick.regionalOptions.af);
})(jQuery);

/* http://keith-wood.name/datepick.html
   Amharic (አማርኛ) localisation for jQuery datepicker.
   Leyu Sisay. */
(function($) {
	'use strict';
	$.datepick.regionalOptions.am = {
		monthNames: ['ጃንዋሪ','ፈብርዋሪ','ማርች','አፕሪል','ሜይ','ጁን',
		'ጁላይ','ኦገስት','ሴፕቴምበር','ኦክቶበር','ኖቬምበር','ዲሴምበር'],
		monthNamesShort: ['ጃንዋ','ፈብር','ማርች','አፕሪ','ሜይ','ጁን',
		'ጁላይ','ኦገስ','ሴፕቴ','ኦክቶ','ኖቬም','ዲሴም'],
		dayNames: ['ሰንዴይ','መንዴይ','ትዩስዴይ','ዌንስዴይ','ተርሰዴይ','ፍራይዴይ','ሳተርዴይ'],
		dayNamesShort: ['ሰንዴ','መንዴ','ትዩስ','ዌንስ','ተርሰ','ፍራይ','ሳተር'],
		dayNamesMin: ['ሰን','መን','ትዩ','ዌን','ተር','ፍራ','ሳተ'],
		dateFormat: 'dd/mm/yyyy',
		firstDay: 1,
		renderer: $.datepick.defaultRenderer,
		prevText: 'ያለፈ',
		prevStatus: 'ያለፈውን ወር አሳይ',
		prevJumpText: '&#x3c;&#x3c;',
		prevJumpStatus: 'ያለፈውን ዓመት አሳይ',
		nextText: 'ቀጣይ',
		nextStatus: 'ቀጣዩን ወር አሳይ',
		nextJumpText: '&#x3e;&#x3e;',
		nextJumpStatus: 'ቀጣዩን ዓመት አሳይ',
		currentText: 'አሁን',
		currentStatus: 'የአሁኑን ወር አሳይ',
		todayText: 'ዛሬ',
		todayStatus: 'የዛሬን ወር አሳይ',
		clearText: 'አጥፋ',
		clearStatus: 'የተመረጠውን ቀን አጥፋ',
		closeText: 'ዝጋ',
		closeStatus: 'የቀን መምረጫውን ዝጋ',
		yearStatus: 'ዓመቱን ቀይር',
		monthStatus: 'ወሩን ቀይር',
		weekText: 'ሳም',
		weekStatus: 'የዓመቱ ሳምንት ',
		dayStatus: 'DD, M d, yyyy ምረጥ',
		defaultStatus: 'ቀን ምረጥ',
		isRTL: false
	};
	$.datepick.setDefaults($.datepick.regionalOptions.am);
})(jQuery);

/* http://keith-wood.name/datepick.html
   Algerian (and Tunisian) Arabic localisation for jQuery Datepicker.
   Mohamed Cherif BOUCHELAGHEM -- cherifbouchelaghem@yahoo.fr */
(function($) {
	'use strict';
	$.datepick.regionalOptions['ar-DZ'] = {
		monthNames: ['جانفي','فيفري','مارس','أفريل','ماي','جوان',
		'جويلية','أوت','سبتمبر','أكتوبر','نوفمبر','ديسمبر'],
		monthNamesShort: ['1','2','3','4','5','6',
		'7','8','9','10','11','12'],
		dayNames: ['الأحد','الاثنين','الثلاثاء','الأربعاء','الخميس','الجمعة','السبت'],
		dayNamesShort: ['الأحد','الاثنين','الثلاثاء','الأربعاء','الخميس','الجمعة','السبت'],
		dayNamesMin: ['الأحد','الاثنين','الثلاثاء','الأربعاء','الخميس','الجمعة','السبت'],
		dateFormat: 'dd/mm/yyyy',
		firstDay: 6,
		renderer: $.datepick.defaultRenderer,
		prevText: '&#x3c;السابق',
		prevStatus: 'عرض الشهر السابق',
		prevJumpText: '&#x3c;&#x3c;',
		prevJumpStatus: '',
		nextText: 'التالي&#x3e;',
		nextStatus: 'عرض الشهر القادم',
		nextJumpText: '&#x3e;&#x3e;',
		nextJumpStatus: '',
		currentText: 'اليوم',
		currentStatus: 'عرض الشهر الحالي',
		todayText: 'اليوم',
		todayStatus: 'عرض الشهر الحالي',
		clearText: 'مسح',
		clearStatus: 'امسح التاريخ الحالي',
		closeText: 'إغلاق',
		closeStatus: 'إغلاق بدون حفظ',
		yearStatus: 'عرض سنة آخرى',
		monthStatus: 'عرض شهر آخر',
		weekText: 'أسبوع',
		weekStatus: 'أسبوع السنة',
		dayStatus: 'اختر D, M d',
		defaultStatus: 'اختر يوم',
		isRTL: true
	};
	$.datepick.setDefaults($.datepick.regionalOptions['ar-DZ']);
})(jQuery);

/* http://keith-wood.name/datepick.html
   Arabic localisation for jQuery Datepicker.
   Mahmoud Khaled -- mahmoud.khaled@badrit.com
   NOTE: monthNames are the new months names */
(function($) {
	'use strict';
	$.datepick.regionalOptions['ar-EG'] = {
		monthNames: ['يناير','فبراير','مارس','إبريل','مايو','يونية',
		'يوليو','أغسطس','سبتمبر','أكتوبر','نوفمبر','ديسمبر'],
		monthNamesShort: ['1','2','3','4','5','6',
		'7','8','9','10','11','12'],
		dayNames:  ['الأحد','الاثنين','الثلاثاء','الأربعاء','الخميس','الجمعة','السبت'],
		dayNamesShort: ['أحد','اثنين','ثلاثاء','أربعاء','خميس','جمعة','سبت'],
		dayNamesMin: ['أحد','اثنين','ثلاثاء','أربعاء','خميس','جمعة','سبت'],
		dateFormat: 'dd/mm/yyyy',
		firstDay: 6,
		renderer: $.datepick.defaultRenderer,
		prevText: '&#x3c;السابق',
		prevStatus: 'عرض الشهر السابق',
		prevJumpText: '&#x3c;&#x3c;',
		prevJumpStatus: '',
		nextText: 'التالي&#x3e;',
		nextStatus: 'عرض الشهر القادم',
		nextJumpText: '&#x3e;&#x3e;',
		nextJumpStatus: '',
		currentText: 'اليوم',
		currentStatus: 'عرض الشهر الحالي',
		todayText: 'اليوم',
		todayStatus: 'عرض الشهر الحالي',
		clearText: 'مسح',
		clearStatus: 'امسح التاريخ الحالي',
		closeText: 'إغلاق',
		closeStatus: 'إغلاق بدون حفظ',
		yearStatus: 'عرض سنة آخرى',
		monthStatus: 'عرض شهر آخر',
		weekText: 'أسبوع',
		weekStatus: 'أسبوع السنة',
		dayStatus: 'اختر D, M d',
		defaultStatus: 'اختر يوم',
		isRTL: true
	};
	$.datepick.setDefaults($.datepick.regionalOptions['ar-EG']);
})(jQuery);

/* http://keith-wood.name/datepick.html
   Arabic localisation for jQuery Datepicker.
   Khaled Al Horani -- koko.dw@gmail.com
   خالد الحوراني -- koko.dw@gmail.com
   NOTE: monthNames are the original months names and they are the Arabic names, not the new months name فبراير - يناير and there isn't any Arabic roots for these months */
(function($) {
	'use strict';
	$.datepick.regionalOptions.ar = {
		monthNames: ['كانون الثاني','شباط','آذار','نيسان','آذار','حزيران',
		'تموز','آب','أيلول','تشرين الأول','تشرين الثاني','كانون الأول'],
		monthNamesShort: ['1','2','3','4','5','6',
		'7','8','9','10','11','12'],
		dayNames: ['الأحد','الاثنين','الثلاثاء','الأربعاء','الخميس','الجمعة','السبت'],
		dayNamesShort: ['الأحد','الاثنين','الثلاثاء','الأربعاء','الخميس','الجمعة','السبت'],
		dayNamesMin: ['الأحد','الاثنين','الثلاثاء','الأربعاء','الخميس','الجمعة','السبت'],
		dateFormat: 'dd/mm/yyyy',
		firstDay: 6,
		renderer: $.datepick.defaultRenderer,
		prevText: '&#x3c;السابق',
		prevStatus: 'عرض الشهر السابق',
		prevJumpText: '&#x3c;&#x3c;',
		prevJumpStatus: '',
		nextText: 'التالي&#x3e;',
		nextStatus: 'عرض الشهر القادم',
		nextJumpText: '&#x3e;&#x3e;',
		nextJumpStatus: '',
		currentText: 'اليوم',
		currentStatus: 'عرض الشهر الحالي',
		todayText: 'اليوم',
		todayStatus: 'عرض الشهر الحالي',
		clearText: 'مسح',
		clearStatus: 'امسح التاريخ الحالي',
		closeText: 'إغلاق',
		closeStatus: 'إغلاق بدون حفظ',
		yearStatus: 'عرض سنة آخرى',
		monthStatus: 'عرض شهر آخر',
		weekText: 'أسبوع',
		weekStatus: 'أسبوع السنة',
		dayStatus: 'اختر D, M d',
		defaultStatus: 'اختر يوم',
		isRTL: true
	};
	$.datepick.setDefaults($.datepick.regionalOptions.ar);
})(jQuery);

/* http://keith-wood.name/datepick.html
   Azerbaijani localisation for jQuery Datepicker.
   Written by Jamil Najafov (necefov33@gmail.com). */
(function($) {
	'use strict';
	$.datepick.regionalOptions.az = {
		monthNames: ['Yanvar','Fevral','Mart','Aprel','May','İyun',
		'İyul','Avqust','Sentyabr','Oktyabr','Noyabr','Dekabr'],
		monthNamesShort: ['Yan','Fev','Mar','Apr','May','İyun',
		'İyul','Avq','Sen','Okt','Noy','Dek'],
		dayNames: ['Bazar','Bazar ertəsi','Çərşənbə axşamı','Çərşənbə','Cümə axşamı','Cümə','Şənbə'],
		dayNamesShort: ['B','Be','Ça','Ç','Ca','C','Ş'],
		dayNamesMin: ['B','B','Ç','С','Ç','C','Ş'],
		dateFormat: 'dd.mm.yyyy',
		firstDay: 1,
		renderer: $.datepick.defaultRenderer,
		prevText: '&#x3c;Geri',
		prevStatus: 'Əvvəlki ay',
		prevJumpText: '&#x3c;&#x3c;',
		prevJumpStatus: 'Əvvəlki il',
		nextText: 'İrəli&#x3e;',
		nextStatus: 'Sonrakı ay',
		nextJumpText: '&#x3e;&#x3e;',
		nextJumpStatus: 'Sonrakı il',
		currentText: 'Bugün',
		currentStatus: 'İndiki ay',
		todayText: 'Bugün',
		todayStatus: 'İndiki ay',
		clearText: 'Təmizlə',
		clearStatus: 'Tarixi sil',
		closeText: 'Bağla',
		closeStatus: 'Təqvimi bağla',
		yearStatus: 'Başqa il',
		monthStatus: 'Başqa ay',
		weekText: 'Hf',
		weekStatus: 'Həftələr',
		dayStatus: 'D, M d seçin',
		defaultStatus: 'Bir tarix seçin',
		isRTL: false
	};
	$.datepick.setDefaults($.datepick.regionalOptions.az);
})(jQuery);
/* http://keith-wood.name/datepick.html
   Bulgarian localisation for jQuery Datepicker.
   Written by Stoyan Kyosev (http://svest.org). */
(function($) {
	'use strict';
	$.datepick.regionalOptions.bg = {
		monthNames: ['Януари','Февруари','Март','Април','Май','Юни',
		'Юли','Август','Септември','Октомври','Ноември','Декември'],
		monthNamesShort: ['Яну','Фев','Мар','Апр','Май','Юни',
		'Юли','Авг','Сеп','Окт','Нов','Дек'],
		dayNames: ['Неделя','Понеделник','Вторник','Сряда','Четвъртък','Петък','Събота'],
		dayNamesShort: ['Нед','Пон','Вто','Сря','Чет','Пет','Съб'],
		dayNamesMin: ['Не','По','Вт','Ср','Че','Пе','Съ'],
		dateFormat: 'dd.mm.yyyy',
		firstDay: 1,
		renderer: $.datepick.defaultRenderer,
		prevText: '&#x3c;назад',
		prevStatus: 'покажи последния месец',
		prevJumpText: '&#x3c;&#x3c;',
		prevJumpStatus: '',
		nextText: 'напред&#x3e;',
		nextStatus: 'покажи следващия месец',
		nextJumpText: '&#x3e;&#x3e;',
		nextJumpStatus: '',
		currentText: 'днес',
		currentStatus: '',
		todayText: 'днес',
		todayStatus: '',
		clearText: 'изчисти',
		clearStatus: 'изчисти актуалната дата',
		closeText: 'затвори',
		closeStatus: 'затвори без промени',
		yearStatus: 'покажи друга година',
		monthStatus: 'покажи друг месец',
		weekText: 'Wk',
		weekStatus: 'седмица от месеца',
		dayStatus: 'Избери D, M d',
		defaultStatus: 'Избери дата',
		isRTL: false
	};
	$.datepick.setDefaults($.datepick.regionalOptions.bg);
})(jQuery);

/* http://keith-wood.name/datepick.html
   Bosnian localisation for jQuery Datepicker.
   Written by Kenan Konjo. */
(function($) {
	'use strict';
	$.datepick.regionalOptions.bs = {
		monthNames: ['Januar','Februar','Mart','April','Maj','Juni',
		'Juli','August','Septembar','Oktobar','Novembar','Decembar'],
		monthNamesShort: ['Jan','Feb','Mar','Apr','Maj','Jun',
		'Jul','Aug','Sep','Okt','Nov','Dec'],
		dayNames: ['Nedelja','Ponedeljak','Utorak','Srijeda','Četvrtak','Petak','Subota'],
		dayNamesShort: ['Ned','Pon','Uto','Sri','Čet','Pet','Sub'],
		dayNamesMin: ['Ne','Po','Ut','Sr','Če','Pe','Su'],
		dateFormat: 'dd.mm.yyyy',
		firstDay: 1,
		renderer: $.datepick.defaultRenderer,
		prevText: '&#x3c;',
		prevStatus: '',
		prevJumpText: '&#x3c;&#x3c;',
		prevJumpStatus: '',
		nextText: '&#x3e;',
		nextStatus: '',
		nextJumpText: '&#x3e;&#x3e;',
		nextJumpStatus: '',
		currentText: 'Danas',
		currentStatus: '',
		todayText: 'Danas',
		todayStatus: '',
		clearText: 'X',
		clearStatus: '',
		closeText: 'Zatvori',
		closeStatus: '',
		yearStatus: '',
		monthStatus: '',
		weekText: 'Wk',
		weekStatus: '',
		dayStatus: 'DD d MM',
		defaultStatus: '',
		isRTL: false
	};
	$.datepick.setDefaults($.datepick.regionalOptions.bs);
})(jQuery);

/* http://keith-wood.name/datepick.html
   Catalan localisation for jQuery Datepicker.
   Writers: (joan.leon@gmail.com). */
(function($) {
	'use strict';
	$.datepick.regionalOptions.ca = {
		monthNames: ['Gener','Febrer','Mar&ccedil;','Abril','Maig','Juny',
		'Juliol','Agost','Setembre','Octubre','Novembre','Desembre'],
		monthNamesShort: ['Gen','Feb','Mar','Abr','Mai','Jun',
		'Jul','Ago','Set','Oct','Nov','Des'],
		dayNames: ['Diumenge','Dilluns','Dimarts','Dimecres','Dijous','Divendres','Dissabte'],
		dayNamesShort: ['Dug','Dln','Dmt','Dmc','Djs','Dvn','Dsb'],
		dayNamesMin: ['Dg','Dl','Dt','Dc','Dj','Dv','Ds'],
		dateFormat: 'dd/mm/yyyy',
		firstDay: 1,
		renderer: $.datepick.defaultRenderer,
		prevText: '&#x3c;Ant',
		prevStatus: '',
		prevJumpText: '&#x3c;&#x3c;',
		prevJumpStatus: '',
		nextText: 'Seg&#x3e;',
		nextStatus: '',
		nextJumpText: '&#x3e;&#x3e;',
		nextJumpStatus: '',
		currentText: 'Avui',
		currentStatus: '',
		todayText: 'Avui',
		todayStatus: '',
		clearText: 'Netejar',
		clearStatus: '',
		closeText: 'Tancar',
		closeStatus: '',
		yearStatus: '',
		monthStatus: '',
		weekText: 'Sm',
		weekStatus: '',
		dayStatus: 'D, M d',
		defaultStatus: '',
		isRTL: false
	};
	$.datepick.setDefaults($.datepick.regionalOptions.ca);
})(jQuery);

/* http://keith-wood.name/datepick.html
   Czech localisation for jQuery Datepicker.
   Written by Tomas Muller (tomas@tomas-muller.net). */
(function($) {
	'use strict';
	$.datepick.regionalOptions.cs = {
		monthNames: ['leden','únor','březen','duben','květen','červen',
		'červenec','srpen','září','říjen','listopad','prosinec'],
		monthNamesShort: ['led','úno','bře','dub','kvě','čer',
		'čvc','srp','zář','říj','lis','pro'],
		dayNames: ['neděle','pondělí','úterý','středa','čtvrtek','pátek','sobota'],
		dayNamesShort: ['ne','po','út','st','čt','pá','so'],
		dayNamesMin: ['ne','po','út','st','čt','pá','so'],
		dateFormat: 'dd.mm.yyyy',
		firstDay: 1,
		renderer: $.datepick.defaultRenderer,
		prevText: '&#x3c;Dříve',
		prevStatus: 'Přejít na předchozí měsí',
		prevJumpText: '&#x3c;&#x3c;',
		prevJumpStatus: '',
		nextText: 'Později&#x3e;',
		nextStatus: 'Přejít na další měsíc',
		nextJumpText: '&#x3e;&#x3e;',
		nextJumpStatus: '',
		currentText: 'Nyní',
		currentStatus: 'Přejde na aktuální měsíc',
		todayText: 'Nyní',
		todayStatus: 'Přejde na aktuální měsíc',
		clearText: 'Vymazat',
		clearStatus: 'Vymaže zadané datum',
		closeText: 'Zavřít',
		closeStatus: 'Zavře kalendář beze změny',
		yearStatus: 'Přejít na jiný rok',
		monthStatus: 'Přejít na jiný měsíc',
		weekText: 'Týd',
		weekStatus: 'Týden v roce',
		dayStatus: '\'Vyber\' DD, M d',
		defaultStatus: 'Vyberte datum',
		isRTL: false
	};
	$.datepick.setDefaults($.datepick.regionalOptions.cs);
})(jQuery);

/* http://keith-wood.name/datepick.html
   Danish localisation for jQuery Datepicker.
   Written by Jan Christensen ( deletestuff@gmail.com). */
(function($) {
	'use strict';
    $.datepick.regionalOptions.da = {
        monthNames: ['Januar','Februar','Marts','April','Maj','Juni',
        'Juli','August','September','Oktober','November','December'],
        monthNamesShort: ['Jan','Feb','Mar','Apr','Maj','Jun',
        'Jul','Aug','Sep','Okt','Nov','Dec'],
		dayNames: ['Søndag','Mandag','Tirsdag','Onsdag','Torsdag','Fredag','Lørdag'],
		dayNamesShort: ['Søn','Man','Tir','Ons','Tor','Fre','Lør'],
		dayNamesMin: ['Sø','Ma','Ti','On','To','Fr','Lø'],
        dateFormat: 'dd-mm-yyyy',
		firstDay: 0,
		renderer: $.datepick.defaultRenderer,
        prevText: '&#x3c;Forrige',
		prevStatus: 'Vis forrige måned',
		prevJumpText: '&#x3c;&#x3c;',
		prevJumpStatus: '',
		nextText: 'Næste&#x3e;',
		nextStatus: 'Vis næste måned',
		nextJumpText: '&#x3e;&#x3e;',
		nextJumpStatus: '',
		currentText: 'Idag',
		currentStatus: 'Vis aktuel måned',
		todayText: 'Idag',
		todayStatus: 'Vis aktuel måned',
		clearText: 'Nulstil',
		clearStatus: 'Nulstil den aktuelle dato',
		closeText: 'Luk',
		closeStatus: 'Luk uden ændringer',
		yearStatus: 'Vis et andet år',
		monthStatus: 'Vis en anden måned',
		weekText: 'Uge',
		weekStatus: 'Årets uge',
		dayStatus: 'Vælg D, M d',
		defaultStatus: 'Vælg en dato',
		isRTL: false
	};
    $.datepick.setDefaults($.datepick.regionalOptions.da);
})(jQuery);

/* http://keith-wood.name/datepick.html
   Swiss-German localisation for jQuery Datepicker.
   Written by Douglas Jose & Juerg Meier. */
(function($) {
	'use strict';
	$.datepick.regionalOptions['de-CH'] = {
		monthNames: ['Januar','Februar','März','April','Mai','Juni',
		'Juli','August','September','Oktober','November','Dezember'],
		monthNamesShort: ['Jan','Feb','Mär','Apr','Mai','Jun',
		'Jul','Aug','Sep','Okt','Nov','Dez'],
		dayNames: ['Sonntag','Montag','Dienstag','Mittwoch','Donnerstag','Freitag','Samstag'],
		dayNamesShort: ['So','Mo','Di','Mi','Do','Fr','Sa'],
		dayNamesMin: ['So','Mo','Di','Mi','Do','Fr','Sa'],
		dateFormat: 'dd.mm.yyyy',
		firstDay: 1,
		renderer: $.datepick.defaultRenderer,
		prevText: '&#x3c;zurück',
		prevStatus: 'letzten Monat zeigen',
		prevJumpText: '&#x3c;&#x3c;',
		prevJumpStatus: '',
		nextText: 'nächster&#x3e;',
		nextStatus: 'nächsten Monat zeigen',
		nextJumpText: '&#x3e;&#x3e;',
		nextJumpStatus: '',
		currentText: 'heute',
		currentStatus: '',
		todayText: 'heute',
		todayStatus: '',
		clearText: 'löschen',
		clearStatus: 'aktuelles Datum löschen',
		closeText: 'schliessen',
		closeStatus: 'ohne Änderungen schliessen',
		yearStatus: 'anderes Jahr anzeigen',
		monthStatus: 'anderen Monat anzeigen',
		weekText: 'Wo',
		weekStatus: 'Woche des Monats',
		dayStatus: 'Wähle D, M d',
		defaultStatus: 'Wähle ein Datum',
		isRTL: false
	};
	$.datepick.setDefaults($.datepick.regionalOptions['de-CH']);
})(jQuery);

/* http://keith-wood.name/datepick.html
   German localisation for jQuery Datepicker.
   Written by Milian Wolff (mail@milianw.de). */
(function($) {
	'use strict';
	$.datepick.regionalOptions.de = {
		monthNames: ['Januar','Februar','März','April','Mai','Juni',
		'Juli','August','September','Oktober','November','Dezember'],
		monthNamesShort: ['Jan','Feb','Mär','Apr','Mai','Jun',
		'Jul','Aug','Sep','Okt','Nov','Dez'],
		dayNames: ['Sonntag','Montag','Dienstag','Mittwoch','Donnerstag','Freitag','Samstag'],
		dayNamesShort: ['So','Mo','Di','Mi','Do','Fr','Sa'],
		dayNamesMin: ['So','Mo','Di','Mi','Do','Fr','Sa'],
		dateFormat: 'dd.mm.yyyy',
		firstDay: 1,
		renderer: $.datepick.defaultRenderer,
		prevText: '&#x3c;zurück',
		prevStatus: 'letzten Monat zeigen',
		prevJumpText: '&#x3c;&#x3c;',
		prevJumpStatus: '',
		nextText: 'Vor&#x3e;',
		nextStatus: 'nächsten Monat zeigen',
		nextJumpText: '&#x3e;&#x3e;',
		nextJumpStatus: '',
		currentText: 'heute',
		currentStatus: '',
		todayText: 'heute',
		todayStatus: '',
		clearText: 'löschen',
		clearStatus: 'aktuelles Datum löschen',
		closeText: 'schließen',
		closeStatus: 'ohne Änderungen schließen',
		yearStatus: 'anderes Jahr anzeigen',
		monthStatus: 'anderen Monat anzeigen',
		weekText: 'Wo',
		weekStatus: 'Woche des Monats',
		dayStatus: 'Wähle D, M d',
		defaultStatus: 'Wähle ein Datum',
		isRTL: false
	};
	$.datepick.setDefaults($.datepick.regionalOptions.de);
})(jQuery);

/* http://keith-wood.name/datepick.html
   Greek localisation for jQuery Datepicker.
   Written by Alex Cicovic (http://www.alexcicovic.com) */
(function($) {
	'use strict';
	$.datepick.regionalOptions.el = {
		monthNames: ['Ιανουάριος','Φεβρουάριος','Μάρτιος','Απρίλιος','Μάιος','Ιούνιος',
		'Ιούλιος','Αύγουστος','Σεπτέμβριος','Οκτώβριος','Νοέμβριος','Δεκέμβριος'],
		monthNamesShort: ['Ιαν','Φεβ','Μαρ','Απρ','Μαι','Ιουν',
		'Ιουλ','Αυγ','Σεπ','Οκτ','Νοε','Δεκ'],
		dayNames: ['Κυριακή','Δευτέρα','Τρίτη','Τετάρτη','Πέμπτη','Παρασκευή','Σάββατο'],
		dayNamesShort: ['Κυρ','Δευ','Τρι','Τετ','Πεμ','Παρ','Σαβ'],
		dayNamesMin: ['Κυ','Δε','Τρ','Τε','Πε','Πα','Σα'],
		dateFormat: 'dd/mm/yyyy',
		firstDay: 1,
		renderer: $.datepick.defaultRenderer,
		prevText: 'Προηγούμενος',
		prevStatus: 'Επισκόπηση προηγούμενου μήνα',
		prevJumpText: '&#x3c;&#x3c;',
		prevJumpStatus: '',
		nextText: 'Επόμενος',
		nextStatus: 'Επισκόπηση επόμενου μήνα',
		nextJumpText: '&#x3e;&#x3e;',
		nextJumpStatus: '',
		currentText: 'Τρέχων Μήνας',
		currentStatus: 'Επισκόπηση τρέχοντος μήνα',
		todayText: 'Τρέχων Μήνας',
		todayStatus: 'Επισκόπηση τρέχοντος μήνα',
		clearText: 'Σβήσιμο',
		clearStatus: 'Σβήσιμο της επιλεγμένης ημερομηνίας',
		closeText: 'Κλείσιμο',
		closeStatus: 'Κλείσιμο χωρίς αλλαγή',
		yearStatus: 'Επισκόπηση άλλου έτους',
		monthStatus: 'Επισκόπηση άλλου μήνα',
		weekText: 'Εβδ',
		weekStatus: '',
		dayStatus: 'Επιλογή DD d MM',
		defaultStatus: 'Επιλέξτε μια ημερομηνία',
		isRTL: false
	};
	$.datepick.setDefaults($.datepick.regionalOptions.el);
})(jQuery);

/* http://keith-wood.name/datepick.html
   English/Australia localisation for jQuery Datepicker.
   Based on en-GB. */
(function($) {
	'use strict';
	$.datepick.regionalOptions['en-AU'] = {
		monthNames: ['January','February','March','April','May','June',
		'July','August','September','October','November','December'],
		monthNamesShort: ['Jan','Feb','Mar','Apr','May','Jun',
		'Jul','Aug','Sep','Oct','Nov','Dec'],
		dayNames: ['Sunday','Monday','Tuesday','Wednesday','Thursday','Friday','Saturday'],
		dayNamesShort: ['Sun','Mon','Tue','Wed','Thu','Fri','Sat'],
		dayNamesMin: ['Su','Mo','Tu','We','Th','Fr','Sa'],
		dateFormat: 'dd/mm/yyyy',
		firstDay: 1,
		renderer: $.datepick.defaultRenderer,
		prevText: 'Prev',
		prevStatus: 'Show the previous month',
		prevJumpText: '&#x3c;&#x3c;',
		prevJumpStatus: 'Show the previous year',
		nextText: 'Next',
		nextStatus: 'Show the next month',
		nextJumpText: '&#x3e;&#x3e;',
		nextJumpStatus: 'Show the next year',
		currentText: 'Current',
		currentStatus: 'Show the current month',
		todayText: 'Today',
		todayStatus: 'Show today\'s month',
		clearText: 'Clear',
		clearStatus: 'Erase the current date',
		closeText: 'Done',
		closeStatus: 'Close without change',
		yearStatus: 'Show a different year',
		monthStatus: 'Show a different month',
		weekText: 'Wk',
		weekStatus: 'Week of the year',
		dayStatus: 'Select DD, M d',
		defaultStatus: 'Select a date',
		isRTL: false
	};
	$.datepick.setDefaults($.datepick.regionalOptions['en-AU']);
})(jQuery);

/* http://keith-wood.name/datepick.html
   English UK localisation for jQuery Datepicker.
   Written by Stuart. */
(function($) {
	'use strict';
	$.datepick.regionalOptions['en-GB'] = {
		monthNames: ['January','February','March','April','May','June',
		'July','August','September','October','November','December'],
		monthNamesShort: ['Jan','Feb','Mar','Apr','May','Jun',
		'Jul','Aug','Sep','Oct','Nov','Dec'],
		dayNames: ['Sunday','Monday','Tuesday','Wednesday','Thursday','Friday','Saturday'],
		dayNamesShort: ['Sun','Mon','Tue','Wed','Thu','Fri','Sat'],
		dayNamesMin: ['Su','Mo','Tu','We','Th','Fr','Sa'],
		dateFormat: 'dd/mm/yyyy',
		firstDay: 1,
		renderer: $.datepick.defaultRenderer,
		prevText: 'Prev',
		prevStatus: 'Show the previous month',
		prevJumpText: '&#x3c;&#x3c;',
		prevJumpStatus: 'Show the previous year',
		nextText: 'Next',
		nextStatus: 'Show the next month',
		nextJumpText: '&#x3e;&#x3e;',
		nextJumpStatus: 'Show the next year',
		currentText: 'Current',
		currentStatus: 'Show the current month',
		todayText: 'Today',
		todayStatus: 'Show today\'s month',
		clearText: 'Clear',
		clearStatus: 'Erase the current date',
		closeText: 'Done',
		closeStatus: 'Close without change',
		yearStatus: 'Show a different year',
		monthStatus: 'Show a different month',
		weekText: 'Wk',
		weekStatus: 'Week of the year',
		dayStatus: 'Select DD, M d',
		defaultStatus: 'Select a date',
		isRTL: false
	};
	$.datepick.setDefaults($.datepick.regionalOptions['en-GB']);
})(jQuery);

/* http://keith-wood.name/datepick.html
   English/New Zealand localisation for jQuery Datepicker.
   Based on en-GB. */
(function($) {
	'use strict';
	$.datepick.regionalOptions['en-NZ'] = {
		monthNames: ['January','February','March','April','May','June',
		'July','August','September','October','November','December'],
		monthNamesShort: ['Jan','Feb','Mar','Apr','May','Jun',
		'Jul','Aug','Sep','Oct','Nov','Dec'],
		dayNames: ['Sunday','Monday','Tuesday','Wednesday','Thursday','Friday','Saturday'],
		dayNamesShort: ['Sun','Mon','Tue','Wed','Thu','Fri','Sat'],
		dayNamesMin: ['Su','Mo','Tu','We','Th','Fr','Sa'],
		dateFormat: 'dd/mm/yyyy',
		firstDay: 1,
		renderer: $.datepick.defaultRenderer,
		prevText: 'Prev',
		prevStatus: 'Show the previous month',
		prevJumpText: '&#x3c;&#x3c;',
		prevJumpStatus: 'Show the previous year',
		nextText: 'Next',
		nextStatus: 'Show the next month',
		nextJumpText: '&#x3e;&#x3e;',
		nextJumpStatus: 'Show the next year',
		currentText: 'Current',
		currentStatus: 'Show the current month',
		todayText: 'Today',
		todayStatus: 'Show today\'s month',
		clearText: 'Clear',
		clearStatus: 'Erase the current date',
		closeText: 'Done',
		closeStatus: 'Close without change',
		yearStatus: 'Show a different year',
		monthStatus: 'Show a different month',
		weekText: 'Wk',
		weekStatus: 'Week of the year',
		dayStatus: 'Select DD, M d',
		defaultStatus: 'Select a date',
		isRTL: false
	};
	$.datepick.setDefaults($.datepick.regionalOptions['en-NZ']);
})(jQuery);

/* http://keith-wood.name/datepick.html
   Esperanto localisation for jQuery Datepicker.
   Written by Olivier M. (olivierweb@ifrance.com). */
(function($) {
	'use strict';
	$.datepick.regionalOptions.eo = {
		monthNames: ['Januaro','Februaro','Marto','Aprilo','Majo','Junio',
		'Julio','Aŭgusto','Septembro','Oktobro','Novembro','Decembro'],
		monthNamesShort: ['Jan','Feb','Mar','Apr','Maj','Jun',
		'Jul','Aŭg','Sep','Okt','Nov','Dec'],
		dayNames: ['Dimanĉo','Lundo','Mardo','Merkredo','Ĵaŭdo','Vendredo','Sabato'],
		dayNamesShort: ['Dim','Lun','Mar','Mer','Ĵaŭ','Ven','Sab'],
		dayNamesMin: ['Di','Lu','Ma','Me','Ĵa','Ve','Sa'],
		dateFormat: 'dd/mm/yyyy',
		firstDay: 0,
		renderer: $.datepick.defaultRenderer,
		prevText: '&lt;Anta',
		prevStatus: 'Vidi la antaŭan monaton',
		prevJumpText: '&#x3c;&#x3c;',
		prevJumpStatus: '',
		nextText: 'Sekv&gt;',
		nextStatus: 'Vidi la sekvan monaton',
		nextJumpText: '&#x3e;&#x3e;',
		nextJumpStatus: '',
		currentText: 'Nuna',
		currentStatus: 'Vidi la nunan monaton',
		todayText: 'Nuna',
		todayStatus: 'Vidi la nunan monaton',
		clearText: 'Vakigi',
		clearStatus: '',
		closeText: 'Fermi',
		closeStatus: 'Fermi sen modifi',
		yearStatus: 'Vidi alian jaron',
		monthStatus: 'Vidi alian monaton',
		weekText: 'Sb',
		weekStatus: '',
		dayStatus: 'Elekti DD, MM d',
		defaultStatus: 'Elekti la daton',
		isRTL: false
	};
	$.datepick.setDefaults($.datepick.regionalOptions.eo);
})(jQuery);

/* http://keith-wood.name/datepick.html
   Spanish/Argentina localisation for jQuery Datepicker.
   Written by Esteban Acosta Villafane (esteban.acosta@globant.com) of Globant (http://www.globant.com). */
(function($) {
	'use strict';
	$.datepick.regionalOptions['es-AR'] = {
		monthNames: ['Enero','Febrero','Marzo','Abril','Mayo','Junio',
		'Julio','Agosto','Septiembre','Octubre','Noviembre','Diciembre'],
		monthNamesShort: ['Ene','Feb','Mar','Abr','May','Jun',
		'Jul','Ago','Sep','Oct','Nov','Dic'],
		dayNames: ['Domingo','Lunes','Martes','Miércoles','Jueves','Viernes','Sábado'],
		dayNamesShort: ['Dom','Lun','Mar','Mié','Juv','Vie','Sáb'],
		dayNamesMin: ['Do','Lu','Ma','Mi','Ju','Vi','Sá'],
		dateFormat: 'dd/mm/yyyy',
		firstDay: 0,
		renderer: $.datepick.defaultRenderer,
		prevText: '&#x3c;Ant',
		prevStatus: '',
		prevJumpText: '&#x3c;&#x3c;',
		prevJumpStatus: '',
		nextText: 'Sig&#x3e;',
		nextStatus: '',
		nextJumpText: '&#x3e;&#x3e;',
		nextJumpStatus: '',
		currentText: 'Hoy',
		currentStatus: '',
		todayText: 'Hoy',
		todayStatus: '',
		clearText: 'Limpiar',
		clearStatus: '',
		closeText: 'Cerrar',
		closeStatus: '',
		yearStatus: '',
		monthStatus: '',
		weekText: 'Sm',
		weekStatus: '',
		dayStatus: 'D, M d',
		defaultStatus: '',
		isRTL: false
	};
	$.datepick.setDefaults($.datepick.regionalOptions['es-AR']);
})(jQuery);

/* http://keith-wood.name/datepick.html
   Spanish/Perú localisation for jQuery Datepicker.
   Written by Fischer Tirado (fishdev@globant.com) of ASIX (http://www.asixonline.com). */
(function($) {
	'use strict';
	$.datepick.regionalOptions['es-PE'] = {
		monthNames: ['Enero','Febrero','Marzo','Abril','Mayo','Junio',
		'Julio','Agosto','Septiembre','Octubre','Noviembre','Diciembre'],
		monthNamesShort: ['Ene','Feb','Mar','Abr','May','Jun',
		'Jul','Ago','Sep','Oct','Nov','Dic'],
		dayNames: ['Domingo','Lunes','Martes','Miércoles','Jueves','Viernes','Sábado'],
		dayNamesShort: ['Dom','Lun','Mar','Mié','Jue','Vie','Sab'],
		dayNamesMin: ['Do','Lu','Ma','Mi','Ju','Vi','Sa'],
		dateFormat: 'dd/mm/yyyy',
		firstDay: 0,
		renderer: $.datepick.defaultRenderer,
		prevText: '&#x3c;Ant',
		prevStatus: '',
		prevJumpText: '&#x3c;&#x3c;',
		prevJumpStatus: '',
		nextText: 'Sig&#x3e;',
		nextStatus: '',
		nextJumpText: '&#x3e;&#x3e;',
		nextJumpStatus: '',
		currentText: 'Hoy',
		currentStatus: '',
		todayText: 'Hoy',
		todayStatus: '',
		clearText: 'Limpiar',
		clearStatus: '',
		closeText: 'Cerrar',
		closeStatus: '',
		yearStatus: '',
		monthStatus: '',
		weekText: 'Sm',
		weekStatus: '',
		dayStatus: 'DD d, MM yyyy',
		defaultStatus: '',
		isRTL: false
	};
	$.datepick.setDefaults($.datepick.regionalOptions['es-PE']);
})(jQuery);

/* http://keith-wood.name/datepick.html
   Spanish localisation for jQuery Datepicker.
   Traducido por Vester (xvester@gmail.com). */
(function($) {
	'use strict';
	$.datepick.regionalOptions.es = {
		monthNames: ['Enero','Febrero','Marzo','Abril','Mayo','Junio',
		'Julio','Agosto','Septiembre','Octubre','Noviembre','Diciembre'],
		monthNamesShort: ['Ene','Feb','Mar','Abr','May','Jun',
		'Jul','Ago','Sep','Oct','Nov','Dic'],
		dayNames: ['Domingo','Lunes','Martes','Miércoles','Jueves','Viernes','Sábado'],
		dayNamesShort: ['Dom','Lun','Mar','Mié','Juv','Vie','Sáb'],
		dayNamesMin: ['Do','Lu','Ma','Mi','Ju','Vi','Sá'],
		dateFormat: 'dd/mm/yyyy',
		firstDay: 1,
		renderer: $.datepick.defaultRenderer,
		prevText: '&#x3c;Ant',
		prevStatus: '',
		prevJumpText: '&#x3c;&#x3c;',
		prevJumpStatus: '',
		nextText: 'Sig&#x3e;',
		nextStatus: '',
		nextJumpText: '&#x3e;&#x3e;',
		nextJumpStatus: '',
		currentText: 'Hoy',
		currentStatus: '',
		todayText: 'Hoy',
		todayStatus: '',
		clearText: 'Limpiar',
		clearStatus: '',
		closeText: 'Cerrar',
		closeStatus: '',
		yearStatus: '',
		monthStatus: '',
		weekText: 'Sm',
		weekStatus: '',
		dayStatus: 'D, M d',
		defaultStatus: '',
		isRTL: false
	};
	$.datepick.setDefaults($.datepick.regionalOptions.es);
})(jQuery);

/* http://keith-wood.name/datepick.html
   Estonian localisation for jQuery Datepicker.
   Written by Mart Sõmermaa (mrts.pydev at gmail com). */ 
(function($) {
	'use strict';
	$.datepick.regionalOptions.et = {
		monthNames: ['Jaanuar','Veebruar','Märts','Aprill','Mai','Juuni',
		'Juuli','August','September','Oktoober','November','Detsember'],
		monthNamesShort: ['Jaan','Veebr','Märts','Apr','Mai','Juuni',
		'Juuli','Aug','Sept','Okt','Nov','Dets'],
		dayNames: ['Pühapäev','Esmaspäev','Teisipäev','Kolmapäev','Neljapäev','Reede','Laupäev'],
		dayNamesShort: ['Pühap','Esmasp','Teisip','Kolmap','Neljap','Reede','Laup'],
		dayNamesMin: ['P','E','T','K','N','R','L'],
		dateFormat: 'dd.mm.yyyy',
		firstDay: 1,
		renderer: $.datepick.defaultRenderer,
		prevText: 'Eelnev',
		prevStatus: '',
		prevJumpText: '&#x3c;&#x3c;',
		prevJumpStatus: '',
		nextText: 'Järgnev',
		nextStatus: '',
		nextJumpText: '&#x3e;&#x3e;',
		nextJumpStatus: '',
		currentText: 'Täna',
		currentStatus: '',
		todayText: 'Täna',
		todayStatus: '',
		clearText: '',
		clearStatus: '',
		closeText: 'Sulge',
		closeStatus: '',
		yearStatus: '',
		monthStatus: '',
		weekText: 'Sm',
		weekStatus: '',
		dayStatus: '',
		defaultStatus: '',
		isRTL: false
	};
	$.datepick.setDefaults($.datepick.regionalOptions.et);
})(jQuery);

/* http://keith-wood.name/datepick.html
   Basque localisation for jQuery Datepicker.
   Karrikas-ek itzulia (karrikas@karrikas.com) */
(function($){
	'use strict';
	$.datepick.regionalOptions.eu = {
		monthNames: ['Urtarrila','Otsaila','Martxoa','Apirila','Maiatza','Ekaina',
		'Uztaila','Abuztua','Iraila','Urria','Azaroa','Abendua'],
		monthNamesShort: ['Urt','Ots','Mar','Api','Mai','Eka',
		'Uzt','Abu','Ira','Urr','Aza','Abe'],
		dayNames: ['Igandea','Astelehena','Asteartea','Asteazkena','Osteguna','Ostirala','Larunbata'],
		dayNamesShort: ['Iga','Ast','Ast','Ast','Ost','Ost','Lar'],
		dayNamesMin: ['Ig','As','As','As','Os','Os','La'],
		dateFormat: 'yyyy/mm/dd',
		firstDay: 1,
		renderer: $.datepick.defaultRenderer,
		prevText: '&#x3c;Aur',
		prevStatus: '',
		prevJumpText: '&#x3c;&#x3c;',
		prevJumpStatus: '',
		nextText: 'Hur&#x3e;',
		nextStatus: '',
		nextJumpText: '&#x3e;&#x3e;',
		nextJumpStatus: '',
		currentText: 'Gaur',
		currentStatus: '',
		todayText: 'Gaur',
		todayStatus: '',
		clearText: 'X',
		clearStatus: '',
		closeText: 'Egina',
		closeStatus: '',
		yearStatus: '',
		monthStatus: '',
		weekText: 'Wk',
		weekStatus: '',
		dayStatus: 'DD d MM',
		defaultStatus: '',
		isRTL: false
	};
	$.datepick.setDefaults($.datepick.regionalOptions.eu);
})(jQuery);

/* http://keith-wood.name/datepick.html
   Persian (Farsi) localisation for jQuery Datepicker.
   Javad Mowlanezhad -- jmowla@gmail.com */
(function($) {
	'use strict';
	/* jshint -W100 */
	$.datepick.regionalOptions.fa = {
		monthNames: ['فروردين','ارديبهشت','خرداد','تير','مرداد','شهريور',
		'مهر','آبان','آذر','دي','بهمن','اسفند'],
		monthNamesShort: ['1','2','3','4','5','6',
		'7','8','9','10','11','12'],
		dayNames: ['يکشنبه','دوشنبه','سه‌شنبه','چهارشنبه','پنجشنبه','جمعه','شنبه'],
		dayNamesShort: ['ي','د','س','چ','پ','ج','ش'],
		dayNamesMin: ['ي','د','س','چ','پ','ج','ش'],
		dateFormat: 'yyyy/mm/dd',
		firstDay: 6,
		renderer: $.datepick.defaultRenderer,
		prevText: '&#x3c;قبلي',
		prevStatus: 'نمايش ماه قبل',
		prevJumpText: '&#x3c;&#x3c;',
		prevJumpStatus: '',
		nextText: 'بعدي&#x3e;',
		nextStatus: 'نمايش ماه بعد',
		nextJumpText: '&#x3e;&#x3e;',
		nextJumpStatus: '',
		currentText: 'امروز',
		currentStatus: 'نمايش ماه جاري',
		todayText: 'امروز',
		todayStatus: 'نمايش ماه جاري',
		clearText: 'حذف تاريخ',
		clearStatus: 'پاک کردن تاريخ جاري',
		closeText: 'بستن',
		closeStatus: 'بستن بدون اعمال تغييرات',
		yearStatus: 'نمايش سال متفاوت',
		monthStatus: 'نمايش ماه متفاوت',
		weekText: 'هف',
		weekStatus: 'هفتهِ سال',
		dayStatus: 'انتخاب D, M d',
		defaultStatus: 'انتخاب تاريخ',
		isRTL: true
	};
	$.datepick.setDefaults($.datepick.regionalOptions.fa);
})(jQuery);

/* http://keith-wood.name/datepick.html
   Finnish localisation for jQuery Datepicker.
   Written by Harri Kilpiö (harrikilpio@gmail.com). */
(function($) {
	'use strict';
	$.datepick.regionalOptions.fi = {
		monthNames: ['Tammikuu','Helmikuu','Maaliskuu','Huhtikuu','Toukokuu','Kes&auml;kuu',
		'Hein&auml;kuu','Elokuu','Syyskuu','Lokakuu','Marraskuu','Joulukuu'],
		monthNamesShort: ['Tammi','Helmi','Maalis','Huhti','Touko','Kes&auml;',
		'Hein&auml;','Elo','Syys','Loka','Marras','Joulu'],
		dayNamesShort: ['Su','Ma','Ti','Ke','To','Pe','Su'],
		dayNames: ['Sunnuntai','Maanantai','Tiistai','Keskiviikko','Torstai','Perjantai','Lauantai'],
		dayNamesMin: ['Su','Ma','Ti','Ke','To','Pe','La'],
		dateFormat: 'dd.mm.yyyy',
		firstDay: 1,
		renderer: $.datepick.defaultRenderer,
		prevText: '&laquo;Edellinen',
		prevStatus: '',
		prevJumpText: '&#x3c;&#x3c;',
		prevJumpStatus: '',
		nextText: 'Seuraava&raquo;',
		nextStatus: '',
		nextJumpText: '&#x3e;&#x3e;',
		nextJumpStatus: '',
		currentText: 'T&auml;n&auml;&auml;n',
		currentStatus: '',
		todayText: 'T&auml;n&auml;&auml;n',
		todayStatus: '',
		clearText: 'Tyhjenn&auml;',
		clearStatus: '',
		closeText: 'Sulje',
		closeStatus: '',
		yearStatus: '',
		monthStatus: '',
		weekText: 'Vk',
		weekStatus: '',
		dayStatus: 'D, M d',
		defaultStatus: '',
		isRTL: false
	};
    $.datepick.setDefaults($.datepick.regionalOptions.fi);
})(jQuery);

/* http://keith-wood.name/datepick.html
   Faroese localisation for jQuery Datepicker.
   Written by Sverri Mohr Olsen, sverrimo@gmail.com */
(function($) {
	'use strict';
	$.datepick.regionalOptions.fo = {
		monthNames: ['Januar','Februar','Mars','Apríl','Mei','Juni',
		'Juli','August','September','Oktober','November','Desember'],
		monthNamesShort: ['Jan','Feb','Mar','Apr','Mei','Jun',
		'Jul','Aug','Sep','Okt','Nov','Des'],
		dayNames: ['Sunnudagur','Mánadagur','Týsdagur','Mikudagur','Hósdagur','Fríggjadagur','Leyardagur'],
		dayNamesShort: ['Sun','Mán','Týs','Mik','Hós','Frí','Ley'],
		dayNamesMin: ['Su','Má','Tý','Mi','Hó','Fr','Le'],
		dateFormat: 'dd-mm-yyyy',
		firstDay: 0,
		renderer: $.datepick.defaultRenderer,
		prevText: '&#x3c;Sísta',
		prevStatus: 'Vís sísta mánaðan',
		prevJumpText: '&#x3c;&#x3c;',
		prevJumpStatus: 'Vís sísta árið',
		nextText: 'Næsta&#x3e;',
		nextStatus: 'Vís næsta mánaðan',
		nextJumpText: '&#x3e;&#x3e;',
		nextJumpStatus: 'Vís næsta árið',
		currentText: 'Í dag',
		currentStatus: 'Vís mánaðan fyri í dag',
		todayText: 'Í dag',
		todayStatus: 'Vís mánaðan fyri í dag',
		clearText: 'Strika',
		clearStatus: 'Strika allir mánaðarnar',
		closeText: 'Goym',
		closeStatus: 'Goym hetta vindeyðga',
		yearStatus: 'Broyt árið',
		monthStatus: 'Broyt mánaðan',
		weekText: 'Vk',
		weekStatus: 'Vika av árinum',
		dayStatus: 'Vel DD, M d, yyyy',
		defaultStatus: 'Vel ein dato',
		isRTL: false
	};
	$.datepick.setDefaults($.datepick.regionalOptions.fo);
})(jQuery);

/* http://keith-wood.name/datepick.html
   Swiss French localisation for jQuery Datepicker.
   Written by Martin Voelkle (martin.voelkle@e-tc.ch). */
(function($) {
	'use strict';
	$.datepick.regionalOptions['fr-CH'] = {
		monthNames: ['Janvier','Février','Mars','Avril','Mai','Juin',
		'Juillet','Août','Septembre','Octobre','Novembre','Décembre'],
		monthNamesShort: ['Jan','Fév','Mar','Avr','Mai','Jun',
		'Jul','Aoû','Sep','Oct','Nov','Déc'],
		dayNames: ['Dimanche','Lundi','Mardi','Mercredi','Jeudi','Vendredi','Samedi'],
		dayNamesShort: ['Dim','Lun','Mar','Mer','Jeu','Ven','Sam'],
		dayNamesMin: ['Di','Lu','Ma','Me','Je','Ve','Sa'],
		dateFormat: 'dd.mm.yyyy',
		firstDay: 1,
		renderer: $.datepick.defaultRenderer,
		prevText: '&#x3c;Préc',
		prevStatus: 'Voir le mois précédent',
		prevJumpText: '&#x3c;&#x3c;',
		prevJumpStatus: '',
		nextText: 'Suiv&#x3e;',
		nextStatus: 'Voir le mois suivant',
		nextJumpText: '&#x3e;&#x3e;',
		nextJumpStatus: '',
		currentText: 'Courant',
		currentStatus: 'Voir le mois courant',
		todayText: 'Aujourd\'hui',
		todayStatus: 'Voir aujourd\'hui',
		clearText: 'Effacer',
		clearStatus: 'Effacer la date sélectionnée',
		closeText: 'Fermer',
		closeStatus: 'Fermer sans modifier',
		yearStatus: 'Voir une autre année',
		monthStatus: 'Voir un autre mois',
		weekText: 'Sm',
		weekStatus: '',
		dayStatus: '\'Choisir\' le DD d MM',
		defaultStatus: 'Choisir la date',
		isRTL: false
	};
	$.datepick.setDefaults($.datepick.regionalOptions['fr-CH']);
})(jQuery);

/* http://keith-wood.name/datepick.html
   French localisation for jQuery Datepicker.
   Stéphane Nahmani (sholby@sholby.net). */
(function($) {
	'use strict';
	$.datepick.regionalOptions.fr = {
		monthNames: ['Janvier','Février','Mars','Avril','Mai','Juin',
		'Juillet','Août','Septembre','Octobre','Novembre','Décembre'],
		monthNamesShort: ['Jan','Fév','Mar','Avr','Mai','Jun',
		'Jul','Aoû','Sep','Oct','Nov','Déc'],
		dayNames: ['Dimanche','Lundi','Mardi','Mercredi','Jeudi','Vendredi','Samedi'],
		dayNamesShort: ['Dim','Lun','Mar','Mer','Jeu','Ven','Sam'],
		dayNamesMin: ['Di','Lu','Ma','Me','Je','Ve','Sa'],
		dateFormat: 'dd/mm/yyyy',
		firstDay: 1,
		renderer: $.datepick.defaultRenderer,
		prevText: '&#x3c;Préc',
		prevStatus: 'Voir le mois précédent',
		prevJumpText: '&#x3c;&#x3c;',
		prevJumpStatus: 'Voir l\'année précédent',
		nextText: 'Suiv&#x3e;',
		nextStatus: 'Voir le mois suivant',
		nextJumpText: '&#x3e;&#x3e;',
		nextJumpStatus: 'Voir l\'année suivant',
		currentText: 'Courant',
		currentStatus: 'Voir le mois courant',
		todayText: 'Aujourd\'hui',
		todayStatus: 'Voir aujourd\'hui',
		clearText: 'Effacer',
		clearStatus: 'Effacer la date sélectionnée',
		closeText: 'Fermer',
		closeStatus: 'Fermer sans modifier',
		yearStatus: 'Voir une autre année',
		monthStatus: 'Voir un autre mois',
		weekText: 'Sm',
		weekStatus: 'Semaine de l\'année',
		dayStatus: '\'Choisir\' le DD d MM',
		defaultStatus: 'Choisir la date',
		isRTL: false
	};
	$.datepick.setDefaults($.datepick.regionalOptions.fr);
})(jQuery);

/* http://keith-wood.name/datepick.html
   Galician localisation for jQuery Datepicker.
   Traducido por Manuel (McNuel@gmx.net). */
(function($) {
	'use strict';
	$.datepick.regionalOptions.gl = {
		monthNames: ['Xaneiro','Febreiro','Marzo','Abril','Maio','Xuño',
		'Xullo','Agosto','Setembro','Outubro','Novembro','Decembro'],
		monthNamesShort: ['Xan','Feb','Mar','Abr','Mai','Xuñ',
		'Xul','Ago','Set','Out','Nov','Dec'],
		dayNames: ['Domingo','Luns','Martes','Mércores','Xoves','Venres','Sábado'],
		dayNamesShort: ['Dom','Lun','Mar','Mér','Xov','Ven','Sáb'],
		dayNamesMin: ['Do','Lu','Ma','Me','Xo','Ve','Sá'],
		dateFormat: 'dd/mm/yyyy',
		firstDay: 1,
		renderer: $.datepick.defaultRenderer,
		prevText: '&#x3c;Ant',
		prevStatus: 'Amosar mes anterior',
		prevJumpText: '&#x3c;&#x3c;',
		prevJumpStatus: 'Amosar ano anterior',
		nextText: 'Seg&#x3e;',
		nextStatus: 'Amosar mes seguinte',
		nextJumpText: '&#x3e;&#x3e;',
		nextJumpStatus: 'Amosar ano seguinte',
		currentText: 'Hoxe',
		currentStatus: 'Amosar mes actual',
		todayText: 'Hoxe',
		todayStatus: 'Amosar mes actual',
		clearText: 'Limpar',
		clearStatus: 'Borrar data actual',
		closeText: 'Pechar',
		closeStatus: 'Pechar sen gardar',
		yearStatus: 'Amosar outro ano',
		monthStatus: 'Amosar outro mes',
		weekText: 'Sm',
		weekStatus: 'Semana do ano',
		dayStatus: 'D, M d',
		defaultStatus: 'Selecciona Data',
		isRTL: false
	};
	$.datepick.setDefaults($.datepick.regionalOptions.gl);
})(jQuery);

/* http://keith-wood.name/datepick.html
   Gujarati (ગુજરાતી) localisation for jQuery Datepicker.
   Naymesh Mistry (naymesh@yahoo.com). */
(function($) {
	'use strict';
	$.datepick.regionalOptions.gu = {
		monthNames: ['જાન્યુઆરી','ફેબ્રુઆરી','માર્ચ','એપ્રિલ','મે','જૂન',
		'જુલાઈ','ઑગસ્ટ','સપ્ટેમ્બર','ઑક્ટોબર','નવેમ્બર','ડિસેમ્બર'],
		monthNamesShort: ['જાન્યુ','ફેબ્રુ','માર્ચ','એપ્રિલ','મે','જૂન',
		'જુલાઈ','ઑગસ્ટ','સપ્ટે','ઑક્ટો','નવે','ડિસે'],
		dayNames: ['રવિવાર','સોમવાર','મંગળવાર','બુધવાર','ગુરુવાર','શુક્રવાર','શનિવાર'],
		dayNamesShort: ['રવિ','સોમ','મંગળ','બુધ','ગુરુ','શુક્ર','શનિ'],
		dayNamesMin: ['ર','સો','મં','બુ','ગુ','શુ','શ'],
		dateFormat: 'dd-M-yyyy',
		firstDay: 1,
		renderer: $.datepick.defaultRenderer,
		prevText: '&#x3c;પાછળ',
		prevStatus: 'પાછલો મહિનો બતાવો',
		prevJumpText: '&#x3c;&#x3c;',
		prevJumpStatus: 'પાછળ',
		nextText: 'આગળ&#x3e;',
		nextStatus: 'આગલો મહિનો બતાવો',
		nextJumpText: '&#x3e;&#x3e;',
		nextJumpStatus: 'આગળ',
		currentText: 'આજે',
		currentStatus: 'આજનો દિવસ બતાવો',
		todayText: 'આજે',
		todayStatus: 'આજનો દિવસ',
		clearText: 'ભૂંસો',
		clearStatus: 'હાલ પસંદ કરેલી તારીખ ભૂંસો',
		closeText: 'બંધ કરો',
		closeStatus: 'તારીખ પસંદ કર્યા વગર બંધ કરો',
		yearStatus: 'જુદુ વર્ષ બતાવો',
		monthStatus: 'જુદો મહિનો બતાવો',
		weekText: 'અઠવાડિયું',
		weekStatus: 'અઠવાડિયું',
		dayStatus: 'અઠવાડિયાનો પહેલો દિવસ પસંદ કરો',
		defaultStatus: 'તારીખ પસંદ કરો',		
		isRTL: false
	};
	$.datepick.setDefaults($.datepick.regionalOptions.gu);
})(jQuery);

/* http://keith-wood.name/datepick.html
   Hebrew localisation for jQuery Datepicker.
   Written by Amir Hardon (ahardon at gmail dot com). */
(function($) {
	'use strict';
	$.datepick.regionalOptions.he = {
		monthNames: ['ינואר','פברואר','מרץ','אפריל','מאי','יוני',
		'יולי','אוגוסט','ספטמבר','אוקטובר','נובמבר','דצמבר'],
		monthNamesShort: ['1','2','3','4','5','6',
		'7','8','9','10','11','12'],
		dayNames: ['ראשון','שני','שלישי','רביעי','חמישי','שישי','שבת'],
		dayNamesShort: ['א\'','ב\'','ג\'','ד\'','ה\'','ו\'','שבת'],
		dayNamesMin: ['א\'','ב\'','ג\'','ד\'','ה\'','ו\'','שבת'],
		dateFormat: 'dd/mm/yyyy',
		firstDay: 0,
		renderer: $.datepick.defaultRenderer,
		prevText: '&#x3c;הקודם',
		prevStatus: '',
		prevJumpText: '&#x3c;&#x3c;',
		prevJumpStatus: '',
		nextText: 'הבא&#x3e;',
		nextStatus: '',
		nextJumpText: '&#x3e;&#x3e;',
		nextJumpStatus: '',
		currentText: 'היום',
		currentStatus: '',
		todayText: 'היום',
		todayStatus: '',
		clearText: 'נקה',
		clearStatus: '',
		closeText: 'סגור',
		closeStatus: '',
		yearStatus: '',
		monthStatus: '',
		weekText: 'Wk',
		weekStatus: '',
		dayStatus: 'DD, M d',
		defaultStatus: '',
		isRTL: true
	};
	$.datepick.setDefaults($.datepick.regionalOptions.he);
})(jQuery);

/* http://keith-wood.name/datepick.html
   Hindi INDIA localisation for jQuery Datepicker.
   Written by Pawan Kumar Singh. */
(function($) {
	'use strict';
	$.datepick.regionalOptions['hi-IN'] = {
		monthNames: ['जनवरी',' फरवरी','मार्च','अप्रैल','मई','जून',
		'जुलाई','अगस्त','सितम्बर','अक्टूबर','नवम्बर','दिसम्बर'],
		monthNamesShort: ['जन','फर','मार्च','अप्रै','मई','जून',
		'जुलाई','अग','सित','अक्टू','नव','दिस'],
		dayNames: ['रविवार','सोमवार','मंगलवार','बुधवार','गुरुवार','शुक्रवार','शनिवार'],
		dayNamesShort: ['रवि','सोम','मंगल','बुध','गुरु','शुक्र','शनि'],
		dayNamesMin: ['र','सो','मं','बु','गु','शु','श'],
		dateFormat: 'dd/mm/yyyy',
		firstDay: 1,
		renderer: $.datepick.defaultRenderer,
		prevText: 'पिछला',
		prevStatus: 'पिछला महीना देखें',
		prevJumpText: '&#x3c;&#x3c;',
		prevJumpStatus: 'पिछला वर्ष देखें',
		nextText: 'अगला',
		nextStatus: 'अगला महीना देखें',
		nextJumpText: '&#x3e;&#x3e;',
		nextJumpStatus: 'अगला वर्ष देखें',
		currentText: 'वर्तमान',
		currentStatus: 'वर्तमान महीना देखें',
		todayText: 'आज',
		todayStatus: 'वर्तमान दिन देखें',
		clearText: 'साफ',
		clearStatus: 'वर्तमान दिनांक मिटाए',
		closeText: 'समाप्त',
		closeStatus: 'बदलाव के बिना बंद',
		yearStatus: 'एक अलग वर्ष का चयन करें',
		monthStatus: 'एक अलग महीने का चयन करें',
		weekText: 'Wk',
		weekStatus: 'वर्ष का सप्ताह',
		dayStatus: 'चुने DD, M d',
		defaultStatus: 'एक तिथि का चयन करें',
		isRTL: false
	};
	$.datepick.setDefaults($.datepick.regionalOptions['hi-IN']);
})(jQuery);

/* http://keith-wood.name/datepick.html
   English Hindi localisation for jQuery Datepicker.
   Written by Tirumal Rao of designphilic.com. */
(function($) {
	'use strict';
	$.datepick.regionalOptions.hi = {
		monthNames: ['जनवरी','फरवरी','मार्च','अप्रैल','मई','जून',
		'जुलाई','अगस्त','सितंबर','अक्टूबर','नवंबर','दिसंबर'],
		monthNamesShort: ['जन','फ़र.','मार्च','अप्रैल','मई','जून',
		'जुलाई','अगस्त','सितंबर','अक्टूबर','नवंबर','दिसंबर'],
		dayNames: ['रविवार','सोमवार','मंगलवार','बुधवार','बृहस्पतिवार','शुक्रवार','शनिवार'],
		dayNamesShort: ['रवि','सोम','मंगल','बुध','बृहस्पत','शुक्र','शनि'],
		dayNamesMin: ['रवि','सोम','मंगल','बुध','बृहस्पत','शुक्र','शनि'],
		dateFormat: 'dd/mm/yyyy',
		firstDay: 1,
		renderer: $.datepick.defaultRenderer,
		prevText: 'पिछला',
		prevStatus: 'पिछले महीने',
		prevJumpText: '&#x3c;&#x3c;',
		prevJumpStatus: 'पिछले वर्ष',
		nextText: 'अगला',
		nextStatus: 'अगले महीने',
		nextJumpText: '&#x3e;&#x3e;',
		nextJumpStatus: 'अगले साल',
		currentText: 'वर्तमान',
		currentStatus: 'चालू माह',
		todayText: 'आज',
		todayStatus: 'आजका महीना',
		clearText: 'साफ़',
		clearStatus: 'वर्तमान दिनांक मिटा',
		closeText: 'ठीक है',
		closeStatus: 'बदलाव के बिना बंद',
		yearStatus: 'एक अलग वर्ष दिखाएँ',
		monthStatus: 'दिखाएँ किसी अन्य महीने के',
		weekText: 'Wk',
		weekStatus: 'Week of the year',
		dayStatus: 'चयन DD, M d',
		defaultStatus: 'एक तिथि का चयन करें',
		isRTL: false
	};
	$.datepick.setDefaults($.datepick.regionalOptions.hi);
})(jQuery);
/* http://keith-wood.name/datepick.html
   Croatian localisation for jQuery Datepicker.
   Written by Vjekoslav Nesek. */
(function($) {
	'use strict';
	$.datepick.regionalOptions.hr = {
		monthNames: ['Siječanj','Veljača','Ožujak','Travanj','Svibanj','Lipanj',
		'Srpanj','Kolovoz','Rujan','Listopad','Studeni','Prosinac'],
		monthNamesShort: ['Sij','Velj','Ožu','Tra','Svi','Lip',
		'Srp','Kol','Ruj','Lis','Stu','Pro'],
		dayNames: ['Nedjelja','Ponedjeljak','Utorak','Srijeda','Četvrtak','Petak','Subota'],
		dayNamesShort: ['Ned','Pon','Uto','Sri','Čet','Pet','Sub'],
		dayNamesMin: ['Ne','Po','Ut','Sr','Če','Pe','Su'],
		dateFormat: 'dd.mm.yyyy.',
		firstDay: 1,
		renderer: $.datepick.defaultRenderer,
		prevText: '&#x3c;',
		prevStatus: 'Prikaži prethodni mjesec',
		prevJumpText: '&#x3c;&#x3c;',
		prevJumpStatus: '',
		nextText: '&#x3e;',
		nextStatus: 'Prikaži slijedeći mjesec',
		nextJumpText: '&#x3e;&#x3e;',
		nextJumpStatus: '',
		currentText: 'Danas',
		currentStatus: 'Današnji datum',
		todayText: 'Danas',
		todayStatus: 'Današnji datum',
		clearText: 'izbriši',
		clearStatus: 'Izbriši trenutni datum',
		closeText: 'Zatvori',
		closeStatus: 'Zatvori kalendar',
		yearStatus: 'Prikaži godine',
		monthStatus: 'Prikaži mjesece',
		weekText: 'Tje',
		weekStatus: 'Tjedan',
		dayStatus: '\'Datum\' D, M d',
		defaultStatus: 'Odaberi datum',
		isRTL: false
	};
	$.datepick.setDefaults($.datepick.regionalOptions.hr);
})(jQuery);

/* http://keith-wood.name/datepick.html
   Hungarian localisation for jQuery Datepicker.
   Written by Istvan Karaszi (jquery@spam.raszi.hu). */
(function($) {
	'use strict';
	$.datepick.regionalOptions.hu = {
		monthNames: ['Január','Február','Március','Április','Május','Június',
		'Július','Augusztus','Szeptember','Október','November','December'],
		monthNamesShort: ['Jan','Feb','Már','Ápr','Máj','Jún',
		'Júl','Aug','Szep','Okt','Nov','Dec'],
		dayNames: ['Vasárnap','Hétfö','Kedd','Szerda','Csütörtök','Péntek','Szombat'],
		dayNamesShort: ['Vas','Hét','Ked','Sze','Csü','Pén','Szo'],
		dayNamesMin: ['V','H','K','Sze','Cs','P','Szo'],
		dateFormat: 'yyyy-mm-dd',
		firstDay: 1,
		renderer: $.datepick.defaultRenderer,
		prevText: '&laquo;&nbsp;vissza',
		prevStatus: '',
		prevJumpText: '&#x3c;&#x3c;',
		prevJumpStatus: '',
		nextText: 'előre&nbsp;&raquo;',
		nextStatus: '',
		nextJumpText: '&#x3e;&#x3e;',
		nextJumpStatus: '',
		currentText: 'ma',
		currentStatus: '',
		todayText: 'ma',
		todayStatus: '',
		clearText: 'törlés',
		clearStatus: '',
		closeText: 'bezárás',
		closeStatus: '',
		yearStatus: '',
		monthStatus: '',
		weekText: 'Hé',
		weekStatus: '',
		dayStatus: 'D, M d',
		defaultStatus: '',
		isRTL: false
	};
	$.datepick.setDefaults($.datepick.regionalOptions.hu);
})(jQuery);

/* http://keith-wood.name/datepick.html
   Armenian localisation for jQuery Datepicker.
   Written by Levon Zakaryan (levon.zakaryan@gmail.com)*/
(function($) {
	'use strict';
	$.datepick.regionalOptions.hy = {
		monthNames: ['Հունվար','Փետրվար','Մարտ','Ապրիլ','Մայիս','Հունիս',
		'Հուլիս','Օգոստոս','Սեպտեմբեր','Հոկտեմբեր','Նոյեմբեր','Դեկտեմբեր'],
		monthNamesShort: ['Հունվ','Փետր','Մարտ','Ապր','Մայիս','Հունիս',
		'Հուլ','Օգս','Սեպ','Հոկ','Նոյ','Դեկ'],
		dayNames: ['կիրակի','եկուշաբթի','երեքշաբթի','չորեքշաբթի','հինգշաբթի','ուրբաթ','շաբաթ'],
		dayNamesShort: ['կիր','երկ','երք','չրք','հնգ','ուրբ','շբթ'],
		dayNamesMin: ['կիր','երկ','երք','չրք','հնգ','ուրբ','շբթ'],
		dateFormat: 'dd.mm.yyyy',
		firstDay: 1,
		renderer: $.datepick.defaultRenderer,
		prevText: '&#x3c;Նախ.',
		prevStatus: '',
		prevJumpText: '&#x3c;&#x3c;',
		prevJumpStatus: '',
		nextText: 'Հաջ.&#x3e;',
		nextStatus: '',
		nextJumpText: '&#x3e;&#x3e;',
		nextJumpStatus: '',
		currentText: 'Այսօր',
		currentStatus: '',
		todayText: 'Այսօր',
		todayStatus: '',
		clearText: 'Մաքրել',
		clearStatus: '',
		closeText: 'Փակել',
		closeStatus: '',
		yearStatus: '',
		monthStatus: '',
		weekText: 'ՇԲՏ',
		weekStatus: '',
		dayStatus: 'D, M d',
		defaultStatus: '',
		isRTL: false
	};
	$.datepick.setDefaults($.datepick.regionalOptions.hy);
})(jQuery);

/* http://keith-wood.name/datepick.html
   Indonesian localisation for jQuery Datepicker.
   Written by Deden Fathurahman (dedenf@gmail.com). */
(function($) {
	'use strict';
	$.datepick.regionalOptions.id = {
		monthNames: ['Januari','Februari','Maret','April','Mei','Juni',
		'Juli','Agustus','September','Oktober','Nopember','Desember'],
		monthNamesShort: ['Jan','Feb','Mar','Apr','Mei','Jun',
		'Jul','Agus','Sep','Okt','Nop','Des'],
		dayNames: ['Minggu','Senin','Selasa','Rabu','Kamis','Jumat','Sabtu'],
		dayNamesShort: ['Min','Sen','Sel','Rab','kam','Jum','Sab'],
		dayNamesMin: ['Mg','Sn','Sl','Rb','Km','jm','Sb'],
		dateFormat: 'dd/mm/yyyy',
		firstDay: 0,
		renderer: $.datepick.defaultRenderer,
		prevText: '&#x3c;mundur',
		prevStatus: 'Tampilkan bulan sebelumnya',
		prevJumpText: '&#x3c;&#x3c;',
		prevJumpStatus: '',
		nextText: 'maju&#x3e;',
		nextStatus: 'Tampilkan bulan berikutnya',
		nextJumpText: '&#x3e;&#x3e;',
		nextJumpStatus: '',
		currentText: 'hari ini',
		currentStatus: 'Tampilkan bulan sekarang',
		todayText: 'hari ini',
		todayStatus: 'Tampilkan bulan sekarang',
		clearText: 'kosongkan',
		clearStatus: 'bersihkan tanggal yang sekarang',
		closeText: 'Tutup',
		closeStatus: 'Tutup tanpa mengubah',
		yearStatus: 'Tampilkan tahun yang berbeda',
		monthStatus: 'Tampilkan bulan yang berbeda',
		weekText: 'Mg',
		weekStatus: 'Minggu dalam tahun',
		dayStatus: 'pilih le DD, MM d',
		defaultStatus: 'Pilih Tanggal',
		isRTL: false
	};
	$.datepick.setDefaults($.datepick.regionalOptions.id);
})(jQuery);

/* http://keith-wood.name/datepick.html
   Icelandic localisation for jQuery Datepicker.
   Written by Haukur H. Thorsson (haukur@eskill.is). */
(function($) {
	'use strict';
	$.datepick.regionalOptions.is = {
		monthNames: ['Jan&uacute;ar','Febr&uacute;ar','Mars','Apr&iacute;l','Ma&iacute','J&uacute;n&iacute;',
		'J&uacute;l&iacute;','&Aacute;g&uacute;st','September','Okt&oacute;ber','N&oacute;vember','Desember'],
		monthNamesShort: ['Jan','Feb','Mar','Apr','Ma&iacute;','J&uacute;n',
		'J&uacute;l','&Aacute;g&uacute;','Sep','Okt','N&oacute;v','Des'],
		dayNames: ['Sunnudagur','M&aacute;nudagur','&THORN;ri&eth;judagur','Mi&eth;vikudagur','Fimmtudagur','F&ouml;studagur','Laugardagur'],
		dayNamesShort: ['Sun','M&aacute;n','&THORN;ri','Mi&eth;','Fim','F&ouml;s','Lau'],
		dayNamesMin: ['Su','M&aacute;','&THORN;r','Mi','Fi','F&ouml;','La'],
		dateFormat: 'dd/mm/yyyy',
		firstDay: 0,
		renderer: $.datepick.defaultRenderer,
		prevText: '&#x3c; Fyrri',
		prevStatus: '',
		prevJumpText: '&#x3c;&#x3c;',
		prevJumpStatus: '',
		nextText: 'N&aelig;sti &#x3e;',
		nextStatus: '',
		nextJumpText: '&#x3e;&#x3e;',
		nextJumpStatus: '',
		currentText: '&Iacute; dag',
		currentStatus: '',
		todayText: '&Iacute; dag',
		todayStatus: '',
		clearText: 'Hreinsa',
		clearStatus: '',
		closeText: 'Loka',
		closeStatus: '',
		yearStatus: '',
		monthStatus: '',
		weekText: 'Vika',
		weekStatus: '',
		dayStatus: 'D, M d',
		defaultStatus: '',
		isRTL: false
	};
	$.datepick.setDefaults($.datepick.regionalOptions.is);
})(jQuery);

/* http://keith-wood.name/datepick.html
   Italian localisation for jQuery Datepicker.
   Written by Apaella (apaella@gmail.com). */
(function($) {
	'use strict';
	$.datepick.regionalOptions.it = {
		monthNames: ['Gennaio','Febbraio','Marzo','Aprile','Maggio','Giugno',
		'Luglio','Agosto','Settembre','Ottobre','Novembre','Dicembre'],
		monthNamesShort: ['Gen','Feb','Mar','Apr','Mag','Giu',
		'Lug','Ago','Set','Ott','Nov','Dic'],
		dayNames: ['Domenica','Lunedì','Martedì','Mercoledì','Giovedì','Venerdì','Sabato'],
		dayNamesShort: ['Dom','Lun','Mar','Mer','Gio','Ven','Sab'],
		dayNamesMin: ['Do','Lu','Ma','Me','Gi','Ve','Sa'],
		dateFormat: 'dd/mm/yyyy',
		firstDay: 1,
		renderer: $.datepick.defaultRenderer,
		prevText: '&#x3c;Prec',
		prevStatus: 'Mese precedente',
		prevJumpText: '&#x3c;&#x3c;',
		prevJumpStatus: 'Mostra l\'anno precedente',
		nextText: 'Succ&#x3e;',
		nextStatus: 'Mese successivo',
		nextJumpText: '&#x3e;&#x3e;',
		nextJumpStatus: 'Mostra l\'anno successivo',
		currentText: 'Oggi',
		currentStatus: 'Mese corrente',
		todayText: 'Oggi',
		todayStatus: 'Mese corrente',
		clearText: 'Svuota',
		clearStatus: 'Annulla',
		closeText: 'Chiudi',
		closeStatus: 'Chiudere senza modificare',
		yearStatus: 'Seleziona un altro anno',
		monthStatus: 'Seleziona un altro mese',
		weekText: 'Sm',
		weekStatus: 'Settimana dell\'anno',
		dayStatus: '\'Seleziona\' D, M d',
		defaultStatus: 'Scegliere una data',
		isRTL: false
	};
	$.datepick.setDefaults($.datepick.regionalOptions.it);
})(jQuery);

/* http://keith-wood.name/datepick.html
   Japanese localisation for jQuery Datepicker.
   Written by Kentaro SATO (kentaro@ranvis.com). */
(function($) {
	'use strict';
	$.datepick.regionalOptions.ja = {
		monthNames: ['1月','2月','3月','4月','5月','6月',
		'7月','8月','9月','10月','11月','12月'],
		monthNamesShort: ['1月','2月','3月','4月','5月','6月',
		'7月','8月','9月','10月','11月','12月'],
		dayNames: ['日曜日','月曜日','火曜日','水曜日','木曜日','金曜日','土曜日'],
		dayNamesShort: ['日','月','火','水','木','金','土'],
		dayNamesMin: ['日','月','火','水','木','金','土'],
		dateFormat: 'yyyy/mm/dd',
		firstDay: 0,
		renderer: $.extend({}, $.datepick.defaultRenderer, {
			month: $.datepick.defaultRenderer.month.replace(/monthHeader/, 'monthHeader:yyyy年 MM')
		}),
		prevText: '&#x3c;前',
		prevStatus: '前月を表示します',
		prevJumpText: '&#x3c;&#x3c;',
		prevJumpStatus: '前年を表示します',
		nextText: '次&#x3e;',
		nextStatus: '翌月を表示します',
		nextJumpText: '&#x3e;&#x3e;',
		nextJumpStatus: '翌年を表示します',
		currentText: '今日',
		currentStatus: '今月を表示します',
		todayText: '今日',
		todayStatus: '今月を表示します',
		clearText: 'クリア',
		clearStatus: '日付をクリアします',
		closeText: '閉じる',
		closeStatus: '変更せずに閉じます',
		yearStatus: '表示する年を変更します',
		monthStatus: '表示する月を変更します',
		weekText: '週',
		weekStatus: '暦週で第何週目かを表します',
		dayStatus: 'Md日(D)',
		defaultStatus: '日付を選択します',
		isRTL: false
	};
	$.datepick.setDefaults($.datepick.regionalOptions.ja);
})(jQuery);

/* http://keith-wood.name/datepick.html
   Georgian localisation for jQuery Datepicker.
   Andrei Gorbushkin. */
(function($) {
	'use strict';
	$.datepick.regionalOptions.ka = {
		monthNames: ['იანვარი','თებერვალი','მარტი','აპრილი','მაისი','ივნისი',
		'ივლისი','აგვისტო','სექტემბერი','ოქტომბერი','ნოემბერი','დეკემბერი'],
		monthNamesShort: ['იან','თებ','მარ','აპრ','მაისი','ივნ',
		'ივლ','აგვ','სექ','ოქტ','ნოე','დეკ'],
		dayNames: ['კვირა','ორშაბათი','სამშაბათი','ოთხშაბათი','ხუთშაბათი','პარასკევი','შაბათი'],
		dayNamesShort: ['კვ','ორშ','სამ','ოთხ','ხუთ','პარ','შაბ'],
		dayNamesMin: ['კვ','ორ','სმ','ოთ','ხშ','პრ','შბ'],
		dateFormat: 'dd/mm/yyyy',
		firstDay: 1,
		renderer: $.datepick.defaultRenderer,
		prevText: '<უკან',
		prevStatus: 'წინა თვე',
		prevJumpText: '&#x3c;&#x3c;',
		prevJumpStatus: 'წინა წელი',
		nextText: 'წინ>',
		nextStatus: 'შემდეგი თვე',
		nextJumpText: '&#x3e;&#x3e;',
		nextJumpStatus: 'შემდეგი წელი',
		currentText: 'მიმდინარე',
		currentStatus: 'მიმდინარე თვე',
		todayText: 'დღეს',
		todayStatus: 'მიმდინარე დღე',
		clearText: 'გასუფთავება',
		clearStatus: 'მიმდინარე თარიღის წაშლა',
		closeText: 'არის',
		closeStatus: 'დახურვა უცვლილებოდ',
		yearStatus: 'სხვა წელი',
		monthStatus: 'სხვა თვე',
		weekText: 'კვ',
		weekStatus: 'წლის კვირა',
		dayStatus: 'აირჩიეთ DD, M d',
		defaultStatus: 'აიღჩიეთ თარიღი',
		isRTL: false
	};
	$.datepick.setDefaults($.datepick.regionalOptions.ka);
})(jQuery);

/* http://keith-wood.name/datepick.html
   Khmer initialisation for jQuery Datepicker.
   Written by Sovichet Tep (sovichet.tep@gmail.com). */
(function($){
	'use strict';
	$.datepick.regionalOptions.km = {
		monthNames: ['ខែ​មករា','ខែ​កុម្ភៈ','ខែ​មិនា','ខែ​មេសា','ខែ​ឧសភា','ខែ​មិថុនា',
		'ខែ​កក្កដា','ខែ​សីហា','ខែ​កញ្ញា','ខែ​តុលា','ខែ​វិច្ឆិកា','ខែ​ធ្នូ'],
		monthNamesShort: ['មក','កុ','មិនា','មេ','ឧស','មិថុ',
		'កក្ក','សី','កញ្ញា','តុលា','វិច្ឆិ','ធ្នូ'],
		dayNames: ['ថ្ងៃ​អាទិត្យ','ថ្ងៃ​ចន្ទ','ថ្ងៃ​អង្គារ','ថ្ងៃ​ពុធ','ថ្ងៃ​ព្រហស្បត្តិ៍','ថ្ងៃ​សុក្រ','ថ្ងៃ​សៅរ៍'],
		dayNamesShort: ['អា','ចន្ទ','អង្គ','ពុធ','ព្រហ','សុ','សៅរ៍'],
		dayNamesMin: ['អា','ច','អ','ពុ','ព្រ','សុ','ស'],
		dateFormat: 'dd/mm/yyyy',
		firstDay: 1,
		renderer: $.datepick.defaultRenderer,
		prevText: 'ថយ​ក្រោយ',
		prevStatus: '',
		prevJumpText: '&#x3c;&#x3c;',
		prevJumpStatus: '',
		nextText: 'ទៅ​មុខ',
		nextStatus: '',
		nextJumpText: '&#x3e;&#x3e;',
		nextJumpStatus: '',
		currentText: 'ថ្ងៃ​នេះ',
		currentStatus: '',
		todayText: 'ថ្ងៃ​នេះ',
		todayStatus: '',
		clearText: 'X',
		clearStatus: '',
		closeText: 'រួច​រាល់',
		closeStatus: '',
		yearStatus: '',
		monthStatus: '',
		weekText: 'Wk',
		weekStatus: '',
		dayStatus: 'DD d MM',
		defaultStatus: '',
		isRTL: false
	};
	$.datepick.setDefaults($.datepick.regionalOptions.km);
})(jQuery);

/* http://keith-wood.name/datepick.html
   Korean localisation for jQuery Datepicker.
   Written by DaeKwon Kang (ncrash.dk@gmail.com), Edited by Genie. */
(function($) {
	'use strict';
	$.datepick.regionalOptions.ko = {
		monthNames: ['1월','2월','3월','4월','5월','6월',
		'7월','8월','9월','10월','11월','12월'],
		monthNamesShort: ['1월','2월','3월','4월','5월','6월',
		'7월','8월','9월','10월','11월','12월'],
		dayNames: ['일요일','월요일','화요일','수요일','목요일','금요일','토요일'],
		dayNamesShort: ['일','월','화','수','목','금','토'],
		dayNamesMin: ['일','월','화','수','목','금','토'],
		dateFormat: 'yyyy-mm-dd',
		firstDay: 0,
		renderer: $.extend({}, $.datepick.defaultRenderer, {
			month: $.datepick.defaultRenderer.month.replace(/monthHeader/, 'monthHeader:yyyy년 MM')
		}),
		prevText: '이전달',
		prevStatus: '이전달을 표시합니다',
		prevJumpText: '&#x3c;&#x3c;',
		prevJumpStatus: '이전 연도를 표시합니다',
		nextText: '다음달',
		nextStatus: '다음달을 표시합니다',
		nextJumpText: '&#x3e;&#x3e;',
		nextJumpStatus: '다음 연도를 표시합니다',
		currentText: '현재',
		currentStatus: '입력한 달을 표시합니다',
		todayText: '오늘',
		todayStatus: '이번달을 표시합니다',
		clearText: '지우기',
		clearStatus: '입력한 날짜를 지웁니다',
		closeText: '닫기',
		closeStatus: '',
		yearStatus: '표시할 연도를 변경합니다',
		monthStatus: '표시할 월을 변경합니다',
		weekText: 'Wk',
		weekStatus: '해당 연도의 주차',
		dayStatus: 'M d일 (D)',
		defaultStatus: '날짜를 선택하세요',
		isRTL: false
	};
	$.datepick.setDefaults($.datepick.regionalOptions.ko);
})(jQuery);

/* http://keith-wood.name/datepick.html
   Lithuanian localisation for jQuery Datepicker.
   Written by Arturas Paleicikas <arturas@avalon.lt> */
(function($) {
	'use strict';
	$.datepick.regionalOptions.lt = {
		monthNames: ['Sausis','Vasaris','Kovas','Balandis','Gegužė','Birželis',
		'Liepa','Rugpjūtis','Rugsėjis','Spalis','Lapkritis','Gruodis'],
		monthNamesShort: ['Sau','Vas','Kov','Bal','Geg','Bir',
		'Lie','Rugp','Rugs','Spa','Lap','Gru'],
		dayNames: ['sekmadienis','pirmadienis','antradienis','trečiadienis','ketvirtadienis','penktadienis','šeštadienis'],
		dayNamesShort: ['sek','pir','ant','tre','ket','pen','šeš'],
		dayNamesMin: ['Se','Pr','An','Tr','Ke','Pe','Še'],
		dateFormat: 'yyyy-mm-dd',
		firstDay: 1,
		renderer: $.datepick.defaultRenderer,
		prevText: '&#x3c;Atgal',
		prevStatus: '',
		prevJumpText: '&#x3c;&#x3c;',
		prevJumpStatus: '',
		nextText: 'Pirmyn&#x3e;',
		nextStatus: '',
		nextJumpText: '&#x3e;&#x3e;',
		nextJumpStatus: '',
		currentText: 'Šiandien',
		currentStatus: '',
		todayText: 'Šiandien',
		todayStatus: '',
		clearText: 'Išvalyti',
		clearStatus: '',
		closeText: 'Uždaryti',
		closeStatus: '',
		yearStatus: '',
		monthStatus: '',
		weekText: 'Wk',
		weekStatus: '',
		dayStatus: 'D, M d',
		defaultStatus: '',
		isRTL: false
	};
	$.datepick.setDefaults($.datepick.regionalOptions.lt);
})(jQuery);

/* http://keith-wood.name/datepick.html
   Latvian localisation for jQuery Datepicker.
   Written by Arturas Paleicikas <arturas.paleicikas@metasite.net> */
(function($) {
	'use strict';
	$.datepick.regionalOptions.lv = {
		monthNames: ['Janvāris','Februāris','Marts','Aprīlis','Maijs','Jūnijs',
		'Jūlijs','Augusts','Septembris','Oktobris','Novembris','Decembris'],
		monthNamesShort: ['Jan','Feb','Mar','Apr','Mai','Jūn',
		'Jūl','Aug','Sep','Okt','Nov','Dec'],
		dayNames: ['svētdiena','pirmdiena','otrdiena','trešdiena','ceturtdiena','piektdiena','sestdiena'],
		dayNamesShort: ['svt','prm','otr','tre','ctr','pkt','sst'],
		dayNamesMin: ['Sv','Pr','Ot','Tr','Ct','Pk','Ss'],
		dateFormat: 'dd-mm-yyyy',
		firstDay: 1,
		renderer: $.datepick.defaultRenderer,
		prevText: 'Iepr',
		prevStatus: '',
		prevJumpText: '&#x3c;&#x3c;',
		prevJumpStatus: '',
		nextText: 'Nāka',
		nextStatus: '',
		nextJumpText: '&#x3e;&#x3e;',
		nextJumpStatus: '',
		currentText: 'Šodien',
		currentStatus: '',
		todayText: 'Šodien',
		todayStatus: '',
		clearText: 'Notīrīt',
		clearStatus: '',
		closeText: 'Aizvērt',
		closeStatus: '',
		yearStatus: '',
		monthStatus: '',
		weekText: 'Nav',
		weekStatus: '',
		dayStatus: 'D, M d',
		defaultStatus: '',
		isRTL: false
	};
	$.datepick.setDefaults($.datepick.regionalOptions.lv);
})(jQuery);

/* http://keith-wood.name/datepick.html
   Montenegrin localisation for jQuery Datepicker.
   By Miloš Milošević - fleka d.o.o. */
(function($) {
	'use strict';
	$.datepick.regionalOptions['me-ME'] = {
		monthNames: ['Januar','Februar','Mart','April','Maj','Jun',
		'Jul','Avgust','Septembar','Oktobar','Novembar','Decembar'],
		monthNamesShort: ['Jan','Feb','Mar','Apr','Maj','Jun',
		'Jul','Avg','Sep','Okt','Nov','Dec'],
		dayNames: ['Neđelja','Poneđeljak','Utorak','Srijeda','Četvrtak','Petak','Subota'],
		dayNamesShort: ['Neđ','Pon','Uto','Sri','Čet','Pet','Sub'],
		dayNamesMin: ['Ne','Po','Ut','Sr','Če','Pe','Su'],
		dateFormat: 'dd/mm/yyyy',
		firstDay: 1,
		renderer: $.datepick.defaultRenderer,
		prevText: '&#x3c;',
		prevStatus: 'Prikaži prethodni mjesec',
		prevJumpText: '&#x3c;&#x3c;',
		prevJumpStatus: 'Prikaži prethodnu godinu',
		nextText: '&#x3e;',
		nextStatus: 'Prikaži sljedeći mjesec',
		nextJumpText: '&#x3e;&#x3e;',
		nextJumpStatus: 'Prikaži sljedeću godinu',
		currentText: 'Danas',
		currentStatus: 'Tekući mjesec',
		todayText: 'Danas',
		todayStatus: 'Tekući mjesec',
		clearText: 'Obriši',
		clearStatus: 'Obriši trenutni datum',
		closeText: 'Zatvori',
		closeStatus: 'Zatvori kalendar',
		yearStatus: 'Prikaži godine',
		monthStatus: 'Prikaži mjesece',
		weekText: 'Sed',
		weekStatus: 'Sedmica',
		dayStatus: '\'Datum\' DD, M d',
		defaultStatus: 'Odaberi datum',
		isRTL: false
	};
	$.datepick.setDefaults($.datepick.regionalOptions['me-ME']);
})(jQuery);

/* http://keith-wood.name/datepick.html
   Montenegrin localisation for jQuery Datepicker.
   By Miloš Milošević - fleka d.o.o. */
(function($) {
	'use strict';
	$.datepick.regionalOptions.me = {
		monthNames: ['Јануар','Фебруар','Март','Април','Мај','Јун',
		'Јул','Август','Септембар','Октобар','Новембар','Децембар'],
		monthNamesShort: ['Јан','Феб','Мар','Апр','Мај','Јун',
		'Јул','Авг','Сеп','Окт','Нов','Дец'],
		dayNames: ['Неђеља','Понеђељак','Уторак','Сриједа','Четвртак','Петак','Субота'],
		dayNamesShort: ['Неђ','Пон','Уто','Сри','Чет','Пет','Суб'],
		dayNamesMin: ['Не','По','Ут','Ср','Че','Пе','Су'],
		dateFormat: 'dd/mm/yyyy',
		firstDay: 1,
		renderer: $.datepick.defaultRenderer,
		prevText: '&#x3c;',
		prevStatus: 'Прикажи претходни мјесец',
		prevJumpText: '&#x3c;&#x3c;',
		prevJumpStatus: 'Прикажи претходну годину',
		nextText: '&#x3e;',
		nextStatus: 'Прикажи сљедећи мјесец',
		nextJumpText: '&#x3e;&#x3e;',
		nextJumpStatus: 'Прикажи сљедећу годину',
		currentText: 'Данас',
		currentStatus: 'Текући мјесец',
		todayText: 'Данас',
		todayStatus: 'Текући мјесец',
		clearText: 'Обриши',
		clearStatus: 'Обриши тренутни датум',
		closeText: 'Затвори',
		closeStatus: 'Затвори календар',
		yearStatus: 'Прикажи године',
		monthStatus: 'Прикажи мјесеце',
		weekText: 'Сед',
		weekStatus: 'Седмица',
		dayStatus: '\'Датум\' DD d MM',
		defaultStatus: 'Одабери датум',
		isRTL: false
	};
	$.datepick.setDefaults($.datepick.regionalOptions.me);
})(jQuery);

/* http://keith-wood.name/datepick.html
   Македонски MK localisation for jQuery Datepicker.
   Written by Hajan Selmani
   email: hajan [at] live [dot] com
   url: http://weblogs.asp.net/hajan | http://codeasp.net/blogs/hajan | http://mkdot.net/blogs/hajan */
(function($) {
	'use strict';
	$.datepick.regionalOptions.mk = {
		monthNames: ['Јануари','Февруари','Март','Април','Мај','Јуни',
		'Јули','Август','Септември','Октомври','Ноември','Декември'],
		monthNamesShort: ['Јан','Фев','Мар','Апр','Мај','Јун',
		'Јул','Авг','Сеп','Окт','Нов','Дек'],
		dayNames: ['Недела','Понеделник','Вторник','Среда','Четврток','Петок','Сабота'],
		dayNamesShort: ['Нед','Пон','Вто','Сре','Чет','Пет','Саб'],
		dayNamesMin: ['Не','По','Вт','Ср','Че','Пе','Са'],
		dateFormat: 'dd/mm/yyyy',
		firstDay: 1,
		renderer: $.datepick.defaultRenderer,
		prevText: 'Претх.',
		prevStatus: 'Прикажи го претходниот месец',
		prevJumpText: '&#x3c;&#x3c;',
		prevJumpStatus: 'Прикажи ја претходната година',
		nextText: 'Следен',
		nextStatus: 'Прикажи го следниот месец',
		nextJumpText: '&#x3e;&#x3e;',
		nextJumpStatus: 'Прикажи ја следната година',
		currentText: 'Тековен',
		currentStatus: 'Прикажи го тековниот месец',
		todayText: 'Денес',
		todayStatus: 'Прикажи го денешниот месец',
		clearText: 'Бриши',
		clearStatus: 'Избриши го тековниот датум',
		closeText: 'Затвори',
		closeStatus: 'Затвори без промени',
		yearStatus: 'Избери друга година',
		monthStatus: 'Избери друг месец',
		weekText: 'Нед',
		weekStatus: 'Недела во годината',
		dayStatus: 'Избери DD, M d',
		defaultStatus: 'Избери датум',
		isRTL: false
	};
	$.datepick.setDefaults($.datepick.regionalOptions.mk);
})(jQuery);
/* http://keith-wood.name/datepick.html
   Malayalam localisation for jQuery Datepicker.
   Saji Nediyanchath (saji89@gmail.com). */
(function($) {
	'use strict';
	/* jshint -W100 */
	$.datepick.regionalOptions.ml = {
		monthNames: ['ജനുവരി','ഫെബ്രുവരി','മാര്‍ച്ച്','ഏപ്രില്‍','മേയ്','ജൂണ്‍',
		'ജൂലൈ','ആഗസ്റ്റ്','സെപ്റ്റംബര്‍','ഒക്ടോബര്‍','നവംബര്‍','ഡിസംബര്‍'],
		monthNamesShort: ['ജനു','ഫെബ്','മാര്‍','ഏപ്രി','മേയ്','ജൂണ്‍',
		'ജൂലാ','ആഗ','സെപ്','ഒക്ടോ','നവം','ഡിസ'],
		dayNames: ['ഞായര്‍','തിങ്കള്‍','ചൊവ്വ','ബുധന്‍','വ്യാഴം','വെള്ളി','ശനി'],
		dayNamesShort: ['ഞായ','തിങ്ക','ചൊവ്വ','ബുധ','വ്യാഴം','വെള്ളി','ശനി'],
		dayNamesMin: ['ഞാ','തി','ചൊ','ബു','വ്യാ','വെ','ശ'],
		dateFormat: 'dd/mm/yyyy',
		firstDay: 1,
		renderer: $.datepick.defaultRenderer,
		prevText: 'മുന്നത്തെ',
		prevStatus: '',
		prevJumpText: '&#x3c;&#x3c;',
		prevJumpStatus: '',
		nextText: 'അടുത്തത് ',
		nextStatus: '',
		nextJumpText: '&#x3e;&#x3e;',
		nextJumpStatus: '',
		currentText: 'ഇന്ന്',
		currentStatus: '',
		todayText: 'ഇന്ന്',
		todayStatus: '',
		clearText: 'X',
		clearStatus: '',
		closeText: 'ശരി',
		closeStatus: '',
		yearStatus: '',
		monthStatus: '',
		weekText: 'ആ',
		weekStatus: '',
		dayStatus: 'DD d MM',
		defaultStatus: '',
		isRTL: false
	};
	$.datepick.setDefaults($.datepick.regionalOptions.ml);
})(jQuery);

/* http://keith-wood.name/datepick.html
   Malaysian localisation for jQuery Datepicker.
   Written by Mohd Nawawi Mohamad Jamili (nawawi@ronggeng.net). */
(function($) {
	'use strict';
	$.datepick.regionalOptions.ms = {
		monthNames: ['Januari','Februari','Mac','April','Mei','Jun',
		'Julai','Ogos','September','Oktober','November','Disember'],
		monthNamesShort: ['Jan','Feb','Mac','Apr','Mei','Jun',
		'Jul','Ogo','Sep','Okt','Nov','Dis'],
		dayNames: ['Ahad','Isnin','Selasa','Rabu','Khamis','Jumaat','Sabtu'],
		dayNamesShort: ['Aha','Isn','Sel','Rab','Kha','Jum','Sab'],
		dayNamesMin: ['Ah','Is','Se','Ra','Kh','Ju','Sa'],
		dateFormat: 'dd/mm/yyyy',
		firstDay: 0,
		renderer: $.datepick.defaultRenderer,
		prevText: '&#x3c;Sebelum',
		prevStatus: 'Tunjukkan bulan lepas',
		prevJumpText: '&#x3c;&#x3c;',
		prevJumpStatus: 'Tunjukkan tahun lepas',
		nextText: 'Selepas&#x3e;',
		nextStatus: 'Tunjukkan bulan depan',
		nextJumpText: '&#x3e;&#x3e;',
		nextJumpStatus: 'Tunjukkan tahun depan',
		currentText: 'hari ini',
		currentStatus: 'Tunjukkan bulan terkini',
		todayText: 'hari ini',
		todayStatus: 'Tunjukkan bulan terkini',
		clearText: 'Padam',
		clearStatus: 'Padamkan tarikh terkini',
		closeText: 'Tutup',
		closeStatus: 'Tutup tanpa perubahan',
		yearStatus: 'Tunjukkan tahun yang lain',
		monthStatus: 'Tunjukkan bulan yang lain',
		weekText: 'Mg',
		weekStatus: 'Minggu bagi tahun ini',
		dayStatus: 'DD, d MM',
		defaultStatus: 'Sila pilih tarikh',
		isRTL: false
	};
	$.datepick.setDefaults($.datepick.regionalOptions.ms);
})(jQuery);

/* http://keith-wood.name/datepick.html
   Maltese localisation for jQuery Datepicker.
   Written by Chritian Sciberras (uuf6429@gmail.com). */
(function($) {
	'use strict';
	$.datepick.regionalOptions.mt = {
		monthNames: ['Jannar','Frar','Marzu','April','Mejju','Ġunju',
		'Lulju','Awissu','Settembru','Ottubru','Novembru','Diċembru'],
		monthNamesShort: ['Jan','Fra','Mar','Apr','Mej','Ġun',
		'Lul','Awi','Set','Ott','Nov','Diċ'],
		dayNames: ['Il-Ħadd','It-Tnejn','It-Tlieta','L-Erbgħa','Il-Ħamis','Il-Ġimgħa','Is-Sibt'],
		dayNamesShort: ['Ħad','Tne','Tli','Erb','Ħam','Ġim','Sib'],
		dayNamesMin: ['Ħ','T','T','E','Ħ','Ġ','S'],
		dateFormat: 'dd/mm/yyyy',
		firstDay: 1,
		renderer: $.datepick.defaultRenderer,
		prevText: 'Ta Qabel',
		prevStatus: 'Ix-xahar ta qabel',
		prevJumpText: '&#x3c;&#x3c;',
		prevJumpStatus: 'Is-sena ta qabel',
		nextText: 'Li Jmiss',
		nextStatus: 'Ix-xahar li jmiss',
		nextJumpText: '&#x3e;&#x3e;',
		nextJumpStatus: 'Is-sena li jmiss',
		currentText: 'Illum',
		currentStatus: 'Ix-xahar ta llum',
		todayText: 'Illum',
		todayStatus: 'Uri ix-xahar ta llum',
		clearText: 'Ħassar',
		clearStatus: 'Ħassar id-data',
		closeText: 'Lest',
		closeStatus: 'Għalaq mingħajr tibdiliet',
		yearStatus: 'Uri sena differenti',
		monthStatus: 'Uri xahar differenti',
		weekText: 'Ġm',
		weekStatus: 'Il-Ġimgħa fis-sena',
		dayStatus: 'Għazel DD, M d',
		defaultStatus: 'Għazel data',
		isRTL: false
	};
	$.datepick.setDefaults($.datepick.regionalOptions.mt);
})(jQuery);

/* http://keith-wood.name/datepick.html
   Dutch/Belgium localisation for jQuery Datepicker.
   Written by Mathias Bynens <http://mathiasbynens.be/> */
(function($) {
	'use strict';
	$.datepick.regionalOptions['nl-BE'] = {
		monthNames: ['januari','februari','maart','april','mei','juni',
		'juli','augustus','september','oktober','november','december'],
		monthNamesShort: ['jan','feb','maa','apr','mei','jun',
		'jul','aug','sep','okt','nov','dec'],
		dayNames: ['zondag','maandag','dinsdag','woensdag','donderdag','vrijdag','zaterdag'],
		dayNamesShort: ['zon','maa','din','woe','don','vri','zat'],
		dayNamesMin: ['zo','ma','di','wo','do','vr','za'],
		dateFormat: 'dd/mm/yyyy',
		firstDay: 1,
		renderer: $.datepick.defaultRenderer,
		prevText: '←',
		prevStatus: 'Bekijk de vorige maand',
		prevJumpText: '«',
		prevJumpStatus: 'Bekijk het vorige jaar',
		nextText: '→',
		nextStatus: 'Bekijk de volgende maand',
		nextJumpText: '»',
		nextJumpStatus: 'Bekijk het volgende jaar',
		currentText: 'Vandaag',
		currentStatus: 'Bekijk de huidige maand',
		todayText: 'Vandaag',
		todayStatus: 'Bekijk de huidige maand',
		clearText: 'Wissen',
		clearStatus: 'Wis de huidige datum',
		closeText: 'Sluiten',
		closeStatus: 'Sluit zonder verandering',
		yearStatus: 'Bekijk een ander jaar',
		monthStatus: 'Bekijk een andere maand',
		weekText: 'Wk',
		weekStatus: 'Week van het jaar',
		dayStatus: 'dd/mm/yyyy',
		defaultStatus: 'Kies een datum',
		isRTL: false
	};
	$.datepick.setDefaults($.datepick.regionalOptions['nl-BE']);
})(jQuery);

/* http://keith-wood.name/datepick.html
   Dutch localisation for jQuery Datepicker.
   Written by Mathias Bynens <http://mathiasbynens.be/> */
(function($) {
	'use strict';
	$.datepick.regionalOptions.nl = {
		monthNames: ['januari','februari','maart','april','mei','juni',
		'juli','augustus','september','oktober','november','december'],
		monthNamesShort: ['jan','feb','maa','apr','mei','jun',
		'jul','aug','sep','okt','nov','dec'],
		dayNames: ['zondag','maandag','dinsdag','woensdag','donderdag','vrijdag','zaterdag'],
		dayNamesShort: ['zon','maa','din','woe','don','vri','zat'],
		dayNamesMin: ['zo','ma','di','wo','do','vr','za'],
		dateFormat: 'dd-mm-yyyy',
		firstDay: 1,
		renderer: $.datepick.defaultRenderer,
		prevText: '←',
		prevStatus: 'Bekijk de vorige maand',
		prevJumpText: '«',
		prevJumpStatus: 'Bekijk het vorige jaar',
		nextText: '→',
		nextStatus: 'Bekijk de volgende maand',
		nextJumpText: '»',
		nextJumpStatus: 'Bekijk het volgende jaar',
		currentText: 'Vandaag',
		currentStatus: 'Bekijk de huidige maand',
		todayText: 'Vandaag',
		todayStatus: 'Bekijk de huidige maand',
		clearText: 'Wissen',
		clearStatus: 'Wis de huidige datum',
		closeText: 'Sluiten',
		closeStatus: 'Sluit zonder verandering',
		yearStatus: 'Bekijk een ander jaar',
		monthStatus: 'Bekijk een andere maand',
		weekText: 'Wk',
		weekStatus: 'Week van het jaar',
		dayStatus: 'dd-mm-yyyy',
		defaultStatus: 'Kies een datum',
		isRTL: false
	};
	$.datepick.setDefaults($.datepick.regionalOptions.nl);
})(jQuery);

/* http://keith-wood.name/datepick.html
   Norwegian localisation for jQuery Datepicker.
   Written by Naimdjon Takhirov (naimdjon@gmail.com). */
(function($) {
	'use strict';
	$.datepick.regionalOptions.no = {
		monthNames: ['Januar','Februar','Mars','April','Mai','Juni',
		'Juli','August','September','Oktober','November','Desember'],
		monthNamesShort: ['Jan','Feb','Mar','Apr','Mai','Jun',
		'Jul','Aug','Sep','Okt','Nov','Des'],
		dayNamesShort: ['Søn','Man','Tir','Ons','Tor','Fre','Lør'],
		dayNames: ['Søndag','Mandag','Tirsdag','Onsdag','Torsdag','Fredag','Lørdag'],
		dayNamesMin: ['Sø','Ma','Ti','On','To','Fr','Lø'],
		dateFormat: 'dd.mm.yyyy',
		firstDay: 1,
		renderer: $.datepick.defaultRenderer,
		prevText: '&laquo;Forrige',
		prevStatus: '',
		prevJumpText: '&#x3c;&#x3c;',
		prevJumpStatus: '',
		nextText: 'Neste&raquo;',
		nextStatus: '',
		nextJumpText: '&#x3e;&#x3e;',
		nextJumpStatus: '',
		currentText: 'I dag',
		currentStatus: '',
		todayText: 'I dag',
		todayStatus: '',
		clearText: 'Tøm',
		clearStatus: '',
		closeText: 'Lukk',
		closeStatus: '',
		yearStatus: '',
		monthStatus: '',
		weekText: 'Uke',
		weekStatus: '',
		dayStatus: 'D, M d',
		defaultStatus: '',
		isRTL: false
	};
	$.datepick.setDefaults($.datepick.regionalOptions.no);
})(jQuery);

/* http://keith-wood.name/datepick.html
   Polish localisation for jQuery Datepicker.
   Written by Jacek Wysocki (jacek.wysocki@gmail.com). */
(function($) {
	'use strict';
	$.datepick.regionalOptions.pl = {
		monthNames: ['Styczeń','Luty','Marzec','Kwiecień','Maj','Czerwiec',
		'Lipiec','Sierpień','Wrzesień','Październik','Listopad','Grudzień'],
		monthNamesShort: ['Sty','Lu','Mar','Kw','Maj','Cze',
		'Lip','Sie','Wrz','Pa','Lis','Gru'],
		dayNames: ['Niedziela','Poniedzialek','Wtorek','Środa','Czwartek','Piątek','Sobota'],
		dayNamesShort: ['Nie','Pn','Wt','Śr','Czw','Pt','So'],
		dayNamesMin: ['N','Pn','Wt','Śr','Cz','Pt','So'],
		dateFormat: 'yyyy-mm-dd',
		firstDay: 1,
		renderer: $.datepick.defaultRenderer,
		prevText: '&#x3c;Poprzedni',
		prevStatus: 'Pokaż poprzedni miesiąc',
		prevJumpText: '&#x3c;&#x3c;',
		prevJumpStatus: '',
		nextText: 'Następny&#x3e;',
		nextStatus: 'Pokaż następny miesiąc',
		nextJumpText: '&#x3e;&#x3e;',
		nextJumpStatus: '',
		currentText: 'Dziś',
		currentStatus: 'Pokaż aktualny miesiąc',
		todayText: 'Dziś',
		todayStatus: 'Pokaż aktualny miesiąc',
		clearText: 'Wyczyść',
		clearStatus: 'Wyczyść obecną datę',
		closeText: 'Zamknij',
		closeStatus: 'Zamknij bez zapisywania',
		yearStatus: 'Pokaż inny rok',
		monthStatus: 'Pokaż inny miesiąc',
		weekText: 'Tydz',
		weekStatus: 'Tydzień roku',
		dayStatus: '\'Wybierz\' D, M d',
		defaultStatus: 'Wybierz datę',
		isRTL: false
	};
	$.datepick.setDefaults($.datepick.regionalOptions.pl);
})(jQuery);

/* http://keith-wood.name/datepick.html
   Brazilian Portuguese localisation for jQuery Datepicker.
   Written by Leonildo Costa Silva (leocsilva@gmail.com). */
(function($) {
	'use strict';
	$.datepick.regionalOptions['pt-BR'] = {
		monthNames: ['Janeiro','Fevereiro','Março','Abril','Maio','Junho',
		'Julho','Agosto','Setembro','Outubro','Novembro','Dezembro'],
		monthNamesShort: ['Jan','Fev','Mar','Abr','Mai','Jun',
		'Jul','Ago','Set','Out','Nov','Dez'],
		dayNames: ['Domingo','Segunda-feira','Terça-feira','Quarta-feira','Quinta-feira','Sexta-feira','Sábado'],
		dayNamesShort: ['Dom','Seg','Ter','Qua','Qui','Sex','Sáb'],
		dayNamesMin: ['D','S','T','Q','Q','S','S'],
		dateFormat: 'dd/mm/yyyy',
		firstDay: 0, 
		renderer: $.datepick.defaultRenderer,
		prevText: '&lt;Anterior',
		prevStatus: 'Mostra o mês anterior',
		prevJumpText: '&lt;&lt;',
		prevJumpStatus: 'Mostra o ano anterior',
		nextText: 'Próximo&gt;',
		nextStatus: 'Mostra o próximo mês',
		nextJumpText: '&gt;&gt;',
		nextJumpStatus: 'Mostra o próximo ano',
		currentText: 'Atual',
		currentStatus: 'Mostra o mês atual',
		todayText: 'Hoje',
		todayStatus: 'Vai para hoje',
		clearText: 'Limpar',
		clearStatus: 'Limpar data',
		closeText: 'Fechar',
		closeStatus: 'Fechar o calendário',
		yearStatus: 'Selecionar ano',
		monthStatus: 'Selecionar mês',
		weekText: 's',
		weekStatus: 'Semana do ano',
		dayStatus: 'DD, d \'de\' M \'de\' yyyy',
		defaultStatus: 'Selecione um dia',
		isRTL: false
	};
	$.datepick.setDefaults($.datepick.regionalOptions['pt-BR']);
})(jQuery);

/* http://keith-wood.name/datepick.html
   Portuguese Portuguese localisation for jQuery Datepicker.
   Written by Telmo Martinho (telmomartinho@gmail.com). */
(function($) {
	'use strict';
	$.datepick.regionalOptions.pt = {
		monthNames: ['Janeiro','Fevereiro','Março','Abril','Maio','Junho',
		'Julho','Agosto','Setembro','Outubro','Novembro','Dezembro'],
		monthNamesShort: ['Jan','Fev','Mar','Abr','Mai','Jun',
		'Jul','Ago','Set','Out','Nov','Dez'],
		dayNames: ['Domingo','Segunda-feira','Terça-feira','Quarta-feira','Quinta-feira','Sexta-feira','Sábado'],
		dayNamesShort: ['Dom','Seg','Ter','Qua','Qui','Sex','Sáb'],
		dayNamesMin: ['D','S','T','Q','Q','S','S'],
		dateFormat: 'dd/mm/yyyy',
		firstDay: 0, 
		renderer: $.datepick.defaultRenderer,
		prevText: '&lt;Anterior',
		prevStatus: 'Mês anterior',
		prevJumpText: '&lt;&lt;',
		prevJumpStatus: 'Ano anterior',
		nextText: 'Próximo&gt;',
		nextStatus: 'Próximo mês',
		nextJumpText: '&gt;&gt;',
		nextJumpStatus: 'Próximo ano',
		currentText: 'Atual',
		currentStatus: 'Mês atual',
		todayText: 'Hoje',
		todayStatus: 'Hoje',
		clearText: 'Limpar',
		clearStatus: 'Limpar data',
		closeText: 'Fechar',
		closeStatus: 'Fechar o calendário',
		yearStatus: 'Selecionar ano',
		monthStatus: 'Selecionar mês',
		weekText: 's',
		weekStatus: 'Semana do ano',
		dayStatus: 'DD, d \'de\' M \'de\' yyyy',
		defaultStatus: 'Selecione um dia',
		isRTL: false
	};
	$.datepick.setDefaults($.datepick.regionalOptions.pt);
})(jQuery);
/* http://keith-wood.name/datepick.html
   Romansh localisation for jQuery Datepicker.
   Yvonne Gienal (yvonne.gienal@educa.ch). */
(function($) {
	'use strict';
	$.datepick.regionalOptions.rm = {
		monthNames: ['Schaner','Favrer','Mars','Avrigl','Matg','Zercladur',
		'Fanadur','Avust','Settember','October','November','December'],
		monthNamesShort: ['Scha','Fev','Mar','Avr','Matg','Zer',
		'Fan','Avu','Sett','Oct','Nov','Dec'],
		dayNames: ['Dumengia','Glindesdi','Mardi','Mesemna','Gievgia','Venderdi','Sonda'],
		dayNamesShort: ['Dum','Gli','Mar','Mes','Gie','Ven','Som'],
		dayNamesMin: ['Du','Gl','Ma','Me','Gi','Ve','So'],
		dateFormat: 'dd/mm/yyyy',
		firstDay: 1,
		renderer: $.datepick.defaultRenderer,
		prevText: '&#x3c;Suandant',
		prevStatus: '',
		prevJumpText: '&#x3c;&#x3c;',
		prevJumpStatus: '',
		nextText: 'Precedent&#x3e;',
		nextStatus: '',
		nextJumpText: '&#x3e;&#x3e;',
		nextJumpStatus: '',
		currentText: 'Actual',
		currentStatus: '',
		todayText: 'Actual',
		todayStatus: '',
		clearText: 'X',
		clearStatus: '',
		closeText: 'Serrar',
		closeStatus: '',
		yearStatus: '',
		monthStatus: '',
		weekText: 'emna',
		weekStatus: '',
		dayStatus: 'DD d MM',
		defaultStatus: '',
		isRTL: false
	};
	$.datepick.setDefaults($.datepick.regionalOptions.rm);
})(jQuery);

/* http://keith-wood.name/datepick.html
   Romanian localisation for jQuery Datepicker.
   Written by Edmond L. (ll_edmond@walla.com) and Ionut G. Stan (ionut.g.stan@gmail.com). */
(function($) {
	'use strict';
	$.datepick.regionalOptions.ro = {
		monthNames: ['Ianuarie','Februarie','Martie','Aprilie','Mai','Iunie',
		'Iulie','August','Septembrie','Octombrie','Noiembrie','Decembrie'],
		monthNamesShort: ['Ian','Feb','Mar','Apr','Mai','Iun',
		'Iul','Aug','Sep','Oct','Noi','Dec'],
		dayNames: ['Duminică','Luni','Marti','Miercuri','Joi','Vineri','Sâmbătă'],
		dayNamesShort: ['Dum','Lun','Mar','Mie','Joi','Vin','Sâm'],
		dayNamesMin: ['Du','Lu','Ma','Mi','Jo','Vi','Sâ'],
		dateFormat: 'dd.mm.yyyy',
		firstDay: 1,
		renderer: $.datepick.defaultRenderer,
		prevText: '&laquo;Precedentă',
		prevStatus: 'Arată luna precedenta',
		prevJumpText: '&laquo;&laquo;',
		prevJumpStatus: '',
		nextText: 'Urmatoare&raquo;',
		nextStatus: 'Arată luna urmatoare',
		nextJumpText: '&raquo;&raquo;',
		nextJumpStatus: '',
		currentText: 'Azi',
		currentStatus: 'Arată luna curenta',
		todayText: 'Azi',
		todayStatus: 'Arată luna curenta',
		clearText: 'Curat',
		clearStatus: 'Sterge data curenta',
		closeText: 'Închide',
		closeStatus: 'Închide fara schimbare',
		yearStatus: 'Arată un an diferit',
		monthStatus: 'Arată o luna diferita',
		weekText: 'Săpt',
		weekStatus: 'Săptamana anului',
		dayStatus: 'Selectează D, M d',
		defaultStatus: 'Selectează o data',
		isRTL: false
	};
	$.datepick.setDefaults($.datepick.regionalOptions.ro);
})(jQuery);

/* http://keith-wood.name/datepick.html
   Russian localisation for jQuery Datepicker.
   Written by Andrew Stromnov (stromnov@gmail.com). */
(function($) {
	'use strict';
	$.datepick.regionalOptions.ru = {
		monthNames: ['Январь','Февраль','Март','Апрель','Май','Июнь',
		'Июль','Август','Сентябрь','Октябрь','Ноябрь','Декабрь'],
		monthNamesShort: ['Янв','Фев','Мар','Апр','Май','Июн',
		'Июл','Авг','Сен','Окт','Ноя','Дек'],
		dayNames: ['воскресенье','понедельник','вторник','среда','четверг','пятница','суббота'],
		dayNamesShort: ['вск','пнд','втр','срд','чтв','птн','сбт'],
		dayNamesMin: ['Вс','Пн','Вт','Ср','Чт','Пт','Сб'],
		dateFormat: 'dd.mm.yyyy',
		firstDay: 1,
		renderer: $.datepick.defaultRenderer,
		prevText: '&#x3c;Пред',
		prevStatus: '',
		prevJumpText: '&#x3c;&#x3c;',
		prevJumpStatus: '',
		nextText: 'След&#x3e;',
		nextStatus: '',
		nextJumpText: '&#x3e;&#x3e;',
		nextJumpStatus: '',
		currentText: 'Сегодня',
		currentStatus: '',
		todayText: 'Сегодня',
		todayStatus: '',
		clearText: 'Очистить',
		clearStatus: '',
		closeText: 'Закрыть',
		closeStatus: '',
		yearStatus: '',
		monthStatus: '',
		weekText: 'Не',
		weekStatus: '',
		dayStatus: 'D, M d',
		defaultStatus: '',
		isRTL: false
	};
	$.datepick.setDefaults($.datepick.regionalOptions.ru);
})(jQuery);

/* http://keith-wood.name/datepick.html
   Slovak localisation for jQuery Datepicker.
   Written by Vojtech Rinik (vojto@hmm.sk). */
(function($) {
	'use strict';
	$.datepick.regionalOptions.sk = {
		monthNames: ['Január','Február','Marec','Apríl','Máj','Jún',
		'Júl','August','September','Október','November','December'],
		monthNamesShort: ['Jan','Feb','Mar','Apr','Máj','Jún',
		'Júl','Aug','Sep','Okt','Nov','Dec'],
		dayNames: ['Nedel\'a','Pondelok','Utorok','Streda','Štvrtok','Piatok','Sobota'],
		dayNamesShort: ['Ned','Pon','Uto','Str','Štv','Pia','Sob'],
		dayNamesMin: ['Ne','Po','Ut','St','Št','Pia','So'],
		dateFormat: 'dd.mm.yyyy',
		firstDay: 0,
		renderer: $.datepick.defaultRenderer,
		prevText: '&#x3c;Predchádzajúci',
		prevStatus: '',
		prevJumpText: '&#x3c;&#x3c;',
		prevJumpStatus: '',
		nextText: 'Nasledujúci&#x3e;',
		nextStatus: '',
		nextJumpText: '&#x3e;&#x3e;',
		nextJumpStatus: '',
		currentText: 'Dnes',
		currentStatus: '',
		todayText: 'Dnes',
		todayStatus: '',
		clearText: 'Zmazať',
		clearStatus: '',
		closeText: 'Zavrieť',
		closeStatus: '',
		yearStatus: '',
		monthStatus: '',
		weekText: 'Ty',
		weekStatus: '',
		dayStatus: 'D, M d',
		defaultStatus: '',
		isRTL: false
	};
	$.datepick.setDefaults($.datepick.regionalOptions.sk);
})(jQuery);

/* http://keith-wood.name/datepick.html
   Slovenian localisation for jQuery Datepicker.
   Written by Jaka Jancar (jaka@kubje.org). */
/* c = &#x10D;, s = &#x161; z = &#x17E; C = &#x10C; S = &#x160; Z = &#x17D; */
(function($) {
	'use strict';
	$.datepick.regionalOptions.sl = {
		monthNames: ['Januar','Februar','Marec','April','Maj','Junij',
		'Julij','Avgust','September','Oktober','November','December'],
		monthNamesShort: ['Jan','Feb','Mar','Apr','Maj','Jun',
		'Jul','Avg','Sep','Okt','Nov','Dec'],
		dayNames: ['Nedelja','Ponedeljek','Torek','Sreda','&#x10C;etrtek','Petek','Sobota'],
		dayNamesShort: ['Ned','Pon','Tor','Sre','&#x10C;et','Pet','Sob'],
		dayNamesMin: ['Ne','Po','To','Sr','&#x10C;e','Pe','So'],
		dateFormat: 'dd.mm.yyyy',
		firstDay: 1,
		renderer: $.datepick.defaultRenderer,
		prevText: '&lt;Prej&#x161;nji',
		prevStatus: 'Prika&#x17E;i prej&#x161;nji mesec',
		prevJumpText: '&#x3c;&#x3c;',
		prevJumpStatus: '',
		nextText: 'Naslednji&gt;',
		nextStatus: 'Prika&#x17E;i naslednji mesec',
		nextJumpText: '&#x3e;&#x3e;',
		nextJumpStatus: '',
		currentText: 'Trenutni',
		currentStatus: 'Prika&#x17E;i trenutni mesec',
		todayText: 'Trenutni',
		todayStatus: 'Prika&#x17E;i trenutni mesec',
		clearText: 'Izbri&#x161;i',
		clearStatus: 'Izbri&#x161;i trenutni datum',
		closeText: 'Zapri',
		closeStatus: 'Zapri brez spreminjanja',
		yearStatus: 'Prika&#x17E;i drugo leto',
		monthStatus: 'Prika&#x17E;i drug mesec',
		weekText: 'Teden',
		weekStatus: 'Teden v letu',
		dayStatus: 'Izberi DD, d MM yy',
		defaultStatus: 'Izbira datuma',
		isRTL: false
	};
	$.datepick.setDefaults($.datepick.regionalOptions.sl);
})(jQuery);

/* http://keith-wood.name/datepick.html
   Albanian localisation for jQuery Datepicker.
   Written by Flakron Bytyqi (flakron@gmail.com). */
(function($) {
	'use strict';
	$.datepick.regionalOptions.sq = {
		monthNames: ['Janar','Shkurt','Mars','Prill','Maj','Qershor',
		'Korrik','Gusht','Shtator','Tetor','Nëntor','Dhjetor'],
		monthNamesShort: ['Jan','Shk','Mar','Pri','Maj','Qer',
		'Kor','Gus','Sht','Tet','Nën','Dhj'],
		dayNames: ['E Diel','E Hënë','E Martë','E Mërkurë','E Enjte','E Premte','E Shtune'],
		dayNamesShort: ['Di','Hë','Ma','Më','En','Pr','Sh'],
		dayNamesMin: ['Di','Hë','Ma','Më','En','Pr','Sh'],
		dateFormat: 'dd.mm.yyyy',
		firstDay: 1,
		renderer: $.datepick.defaultRenderer,
		prevText: '&#x3c;mbrapa',
		prevStatus: 'trego muajin e fundit',
		prevJumpText: '&#x3c;&#x3c;',
		prevJumpStatus: '',
		nextText: 'Përpara&#x3e;',
		nextStatus: 'trego muajin tjetër',
		nextJumpText: '&#x3e;&#x3e;',
		nextJumpStatus: '',
		currentText: 'sot',
		currentStatus: '',
		todayText: 'sot',
		todayStatus: '',
		clearText: 'fshije',
		clearStatus: 'fshije datën aktuale',
		closeText: 'mbylle',
		closeStatus: 'mbylle pa ndryshime',
		yearStatus: 'trego tjetër vit',
		monthStatus: 'trego muajin tjetër',
		weekText: 'Ja',
		weekStatus: 'Java e muajit',
		dayStatus: '\'Zgjedh\' D, M d',
		defaultStatus: 'Zgjedhe një datë',
		isRTL: false
	};
	$.datepick.setDefaults($.datepick.regionalOptions.sq);
})(jQuery);

/* http://keith-wood.name/datepick.html
   Serbian localisation for jQuery Datepicker.
   Written by Dejan Dimić. */
(function($){
	'use strict';
	$.datepick.regionalOptions['sr-SR'] = {
		monthNames: ['Januar','Februar','Mart','April','Maj','Jun',
		'Jul','Avgust','Septembar','Oktobar','Novembar','Decembar'],
		monthNamesShort: ['Jan','Feb','Mar','Apr','Maj','Jun',
		'Jul','Avg','Sep','Okt','Nov','Dec'],
		dayNames: ['Nedelja','Ponedeljak','Utorak','Sreda','Četvrtak','Petak','Subota'],
		dayNamesShort: ['Ned','Pon','Uto','Sre','Čet','Pet','Sub'],
		dayNamesMin: ['Ne','Po','Ut','Sr','Če','Pe','Su'],
		dateFormat: 'dd/mm/yyyy',
		firstDay: 1,
		renderer: $.datepick.defaultRenderer,
		prevText: '&#x3c;',
		prevStatus: 'Prikaži prethodni mesec',
		prevJumpText: '&#x3c;&#x3c;',
		prevJumpStatus: 'Prikaži prethodnu godinu',
		nextText: '&#x3e;',
		nextStatus: 'Prikaži sledeći mesec',
		nextJumpText: '&#x3e;&#x3e;',
		nextJumpStatus: 'Prikaži sledeću godinu',
		currentText: 'Danas',
		currentStatus: 'Tekući mesec',
		todayText: 'Danas',
		todayStatus: 'Tekući mesec',
		clearText: 'Obriši',
		clearStatus: 'Obriši trenutni datum',
		closeText: 'Zatvori',
		closeStatus: 'Zatvori kalendar',
		yearStatus: 'Prikaži godine',
		monthStatus: 'Prikaži mesece',
		weekText: 'Sed',
		weekStatus: 'Sedmica',
		dayStatus: '\'Datum\' D, M d',
		defaultStatus: 'Odaberi datum',
		isRTL: false
	};
	$.datepick.setDefaults($.datepick.regionalOptions['sr-SR']);
})(jQuery);

/* http://keith-wood.name/datepick.html
   Serbian localisation for jQuery Datepicker.
   Written by Dejan Dimić. */
(function($) {
	'use strict';
	$.datepick.regionalOptions.sr = {
		monthNames: ['Јануар','Фебруар','Март','Април','Мај','Јун',
		'Јул','Август','Септембар','Октобар','Новембар','Децембар'],
		monthNamesShort: ['Јан','Феб','Мар','Апр','Мај','Јун',
		'Јул','Авг','Сеп','Окт','Нов','Дец'],
		dayNames: ['Недеља','Понедељак','Уторак','Среда','Четвртак','Петак','Субота'],
		dayNamesShort: ['Нед','Пон','Уто','Сре','Чет','Пет','Суб'],
		dayNamesMin: ['Не','По','Ут','Ср','Че','Пе','Су'],
		dateFormat: 'dd/mm/yyyy',
		firstDay: 1,
		renderer: $.datepick.defaultRenderer,
		prevText: '&#x3c;',
		prevStatus: 'Прикажи претходни месец',
		prevJumpText: '&#x3c;&#x3c;',
		prevJumpStatus: 'Прикажи претходну годину',
		nextText: '&#x3e;',
		nextStatus: 'Прикажи следећи месец',
		nextJumpText: '&#x3e;&#x3e;',
		nextJumpStatus: 'Прикажи следећу годину',
		currentText: 'Данас',
		currentStatus: 'Текући месец',
		todayText: 'Данас',
		todayStatus: 'Текући месец',
		clearText: 'Обриши',
		clearStatus: 'Обриши тренутни датум',
		closeText: 'Затвори',
		closeStatus: 'Затвори календар',
		yearStatus: 'Прикажи године',
		monthStatus: 'Прикажи месеце',
		weekText: 'Сед',
		weekStatus: 'Седмица',
		dayStatus: '\'Датум\' DD d MM',
		defaultStatus: 'Одабери датум',
		isRTL: false
	};
	$.datepick.setDefaults($.datepick.regionalOptions.sr);
})(jQuery);

/* http://keith-wood.name/datepick.html
   Swedish localisation for jQuery Datepicker.
   Written by Anders Ekdahl ( anders@nomadiz.se). */
(function($) {
	'use strict';
    $.datepick.regionalOptions.sv = {
        monthNames: ['Januari','Februari','Mars','April','Maj','Juni',
        'Juli','Augusti','September','Oktober','November','December'],
        monthNamesShort: ['Jan','Feb','Mar','Apr','Maj','Jun',
        'Jul','Aug','Sep','Okt','Nov','Dec'],
		dayNames: ['Söndag','Måndag','Tisdag','Onsdag','Torsdag','Fredag','Lördag'],
		dayNamesShort: ['Sön','Mån','Tis','Ons','Tor','Fre','Lör'],
		dayNamesMin: ['Sö','Må','Ti','On','To','Fr','Lö'],
        dateFormat: 'yyyy-mm-dd',
		firstDay: 1,
		renderer: $.datepick.defaultRenderer,
        prevText: '&laquo;Förra',
		prevStatus: '',
		prevJumpText: '&#x3c;&#x3c;',
		prevJumpStatus: '',
		nextText: 'Nästa&raquo;',
		nextStatus: '',
		nextJumpText: '&#x3e;&#x3e;',
		nextJumpStatus: '',
		currentText: 'Idag',
		currentStatus: '',
		todayText: 'Idag',
		todayStatus: '',
		clearText: 'Rensa',
		clearStatus: '',
		closeText: 'Stäng',
		closeStatus: '',
		yearStatus: '',
		monthStatus: '',
		weekText: 'Ve',
		weekStatus: '',
		dayStatus: 'D, M d',
		defaultStatus: '',
		isRTL: false
	};
    $.datepick.setDefaults($.datepick.regionalOptions.sv);
})(jQuery);

/* http://keith-wood.name/datepick.html
   Tamil localisation for jQuery Datepicker.
   Written by S A Sureshkumar (saskumar@live.com). */
(function($) {
	'use strict';
	$.datepick.regionalOptions.ta = {
		monthNames: ['தை','மாசி','பங்குனி','சித்திரை','வைகாசி','ஆனி',
		'ஆடி','ஆவணி','புரட்டாசி','ஐப்பசி','கார்த்திகை','மார்கழி'],
		monthNamesShort: ['தை','மாசி','பங்','சித்','வைகா','ஆனி',
		'ஆடி','ஆவ','புர','ஐப்','கார்','மார்'],
		dayNames: ['ஞாயிற்றுக்கிழமை','திங்கட்கிழமை','செவ்வாய்க்கிழமை','புதன்கிழமை','வியாழக்கிழமை','வெள்ளிக்கிழமை','சனிக்கிழமை'],
		dayNamesShort: ['ஞாயிறு','திங்கள்','செவ்வாய்','புதன்','வியாழன்','வெள்ளி','சனி'],
		dayNamesMin: ['ஞா','தி','செ','பு','வி','வெ','ச'],
		dateFormat: 'dd/mm/yyyy',
		firstDay: 1,
		renderer: $.datepick.defaultRenderer,
		prevText: 'முன்னையது',
		prevStatus: '',
		prevJumpText: '&#x3c;&#x3c;',
		prevJumpStatus: '',
		nextText: 'அடுத்தது',
		nextStatus: '',
		nextJumpText: '&#x3e;&#x3e;',
		nextJumpStatus: '',
		currentText: 'இன்று',
		currentStatus: '',
		todayText: 'இன்று',
		todayStatus: '',
		clearText: 'அழி',
		clearStatus: '',
		closeText: 'மூடு',
		closeStatus: '',
		yearStatus: '',
		monthStatus: '',
		weekText: 'Wk',
		weekStatus: '',
		dayStatus: 'D, M d',
		defaultStatus: '',
		isRTL: false
	};
	$.datepick.setDefaults($.datepick.regionalOptions.ta);
})(jQuery);

/* http://keith-wood.name/datepick.html
   Thai localisation for jQuery Datepicker.
   Written by pipo (pipo@sixhead.com). */
(function($) {
	'use strict';
	$.datepick.regionalOptions.th = {
		monthNames: ['มกราคม','กุมภาพันธ์','มีนาคม','เมษายน','พฤษภาคม','มิถุนายน',
		'กรกฎาคม','สิงหาคม','กันยายน','ตุลาคม','พฤศจิกายน','ธันวาคม'],
		monthNamesShort: ['ม.ค.','ก.พ.','มี.ค.','เม.ย.','พ.ค.','มิ.ย.',
		'ก.ค.','ส.ค.','ก.ย.','ต.ค.','พ.ย.','ธ.ค.'],
		dayNames: ['อาทิตย์','จันทร์','อังคาร','พุธ','พฤหัสบดี','ศุกร์','เสาร์'],
		dayNamesShort: ['อา.','จ.','อ.','พ.','พฤ.','ศ.','ส.'],
		dayNamesMin: ['อา.','จ.','อ.','พ.','พฤ.','ศ.','ส.'],
		dateFormat: 'dd/mm/yyyy',
		firstDay: 0,
		renderer: $.datepick.defaultRenderer,
		prevText: '&laquo;&nbsp;ย้อน',
		prevStatus: '',
		prevJumpText: '&#x3c;&#x3c;',
		prevJumpStatus: '',
		nextText: 'ถัดไป&nbsp;&raquo;',
		nextStatus: '',
		nextJumpText: '&#x3e;&#x3e;',
		nextJumpStatus: '',
		currentText: 'วันนี้',
		currentStatus: '',
		todayText: 'วันนี้',
		todayStatus: '',
		clearText: 'ลบ',
		clearStatus: '',
		closeText: 'ปิด',
		closeStatus: '',
		yearStatus: '',
		monthStatus: '',
		weekText: 'Wk',
		weekStatus: '',
		dayStatus: 'D, M d',
		defaultStatus: '',
		isRTL: false
	};
	$.datepick.setDefaults($.datepick.regionalOptions.th);
})(jQuery);

/* http://keith-wood.name/datepick.html
   Turkish localisation for jQuery Datepicker.
   Written by Izzet Emre Erkan (kara@karalamalar.net). */
(function($) {
	'use strict';
	$.datepick.regionalOptions.tr = {
		monthNames: ['Ocak','Şubat','Mart','Nisan','Mayıs','Haziran',
		'Temmuz','Ağustos','Eylül','Ekim','Kasım','Aralık'],
		monthNamesShort: ['Oca','Şub','Mar','Nis','May','Haz',
		'Tem','Ağu','Eyl','Eki','Kas','Ara'],
		dayNames: ['Pazar','Pazartesi','Salı','Çarşamba','Perşembe','Cuma','Cumartesi'],
		dayNamesShort: ['Pz','Pt','Sa','Ça','Pe','Cu','Ct'],
		dayNamesMin: ['Pz','Pt','Sa','Ça','Pe','Cu','Ct'],
		dateFormat: 'dd.mm.yyyy',
		firstDay: 1,
		renderer: $.datepick.defaultRenderer,
		prevText: '&#x3c;geri',
		prevStatus: 'önceki ayı göster',
		prevJumpText: '&#x3c;&#x3c;',
		prevJumpStatus: '',
		nextText: 'ileri&#x3e',
		nextStatus: 'sonraki ayı göster',
		nextJumpText: '&#x3e;&#x3e;',
		nextJumpStatus: '',
		currentText: 'bugün',
		currentStatus: '',
		todayText: 'bugün',
		todayStatus: '',
		clearText: 'temizle',
		clearStatus: 'geçerli tarihi temizler',
		closeText: 'kapat',
		closeStatus: 'sadece göstergeyi kapat',
		yearStatus: 'başka yıl',
		monthStatus: 'başka ay',
		weekText: 'Hf',
		weekStatus: 'Ayın haftaları',
		dayStatus: 'D, M d seçiniz',
		defaultStatus: 'Bir tarih seçiniz',
		isRTL: false
	};
	$.datepick.setDefaults($.datepick.regionalOptions.tr);
})(jQuery);

/* http://keith-wood.name/datepick.html
   Tatar localisation for jQuery Datepicker.
   Written by Irek Khaziev (khazirek@gmail.com). */
(function($) {
	'use strict';
	$.datepick.regionalOptions.tt = {
		monthNames: ['Гынвар','Февраль','Март','Апрель','Май','Июнь',
		'Июль','Август','Сентябрь','Октябрь','Ноябрь','Декабрь'],
		monthNamesShort: ['Гыйн','Фев','Мар','Апр','Май','Июн',
		'Июл','Авг','Сен','Окт','Ноя','Дек'],
		dayNames: ['якшәмбе','дүшәмбе','сишәмбе','чәршәмбе','пәнҗешәмбе','җомга','шимбә'],
		dayNamesShort: ['якш','дүш','сиш','чәр','пән','җом','шим'],
		dayNamesMin: ['Як','Дү','Си','Чә','Пә','Җо','Ши'],
		dateFormat: 'dd.mm.yyyy',
		firstDay: 1,
		renderer: $.datepick.defaultRenderer,
		prevText: 'Алдагы',
		prevStatus: 'Алдагы айны күрсәтү',
		prevJumpText: '&lt;&lt;',
		prevJumpStatus: 'Алдагы елны күрсәтү',
		nextText: 'Киләсе',
		nextStatus: 'Киләсе айны күрсәтү',
		nextJumpText: '&gt;&gt;',
		nextJumpStatus: 'Киләсе елны күрсәтү',
		currentText: 'Хәзер',
		currentStatus: 'Хәзерге айны күрсәтү',
		todayText: 'Бүген',
		todayStatus: 'Бүгенге айны күрсәтү',
		clearText: 'Чистарту',
		clearStatus: 'Барлык көннәрне чистарту',
		closeText: 'Ябарга',
		closeStatus: 'Көн сайлауны ябарга',
		yearStatus: 'Елны кертегез',
		monthStatus: 'Айны кертегез',
		weekText: 'Атна',
		weekStatus: 'Елда атна саны',
		dayStatus: 'DD, M d',
		defaultStatus: 'Көнне сайлагыз',
		isRTL: false
	};
	$.datepick.setDefaults($.datepick.regionalOptions.tt);
})(jQuery);

/* http://keith-wood.name/datepick.html
   Ukrainian localisation for jQuery Datepicker.
   Written by Maxim Drogobitskiy (maxdao@gmail.com). */
(function($) {
	'use strict';
	$.datepick.regionalOptions.uk = {
		monthNames: ['Січень','Лютий','Березень','Квітень','Травень','Червень',
		'Липень','Серпень','Вересень','Жовтень','Листопад','Грудень'],
		monthNamesShort: ['Січ','Лют','Бер','Кві','Тра','Чер',
		'Лип','Сер','Вер','Жов','Лис','Гру'],
		dayNames: ['неділя','понеділок','вівторок','середа','четвер','п\'ятниця','субота'],
		dayNamesShort: ['нед','пнд','вів','срд','чтв','птн','сбт'],
		dayNamesMin: ['Нд','Пн','Вт','Ср','Чт','Пт','Сб'],
		dateFormat: 'dd/mm/yyyy',
		firstDay: 1,
		renderer: $.datepick.defaultRenderer,
		prevText: '&#x3c;',
		prevStatus: '',
		prevJumpText: '&#x3c;&#x3c;',
		prevJumpStatus: '',
		nextText: '&#x3e;',
		nextStatus: '',
		nextJumpText: '&#x3e;&#x3e;',
		nextJumpStatus: '',
		currentText: 'Сьогодні',
		currentStatus: '',
		todayText: 'Сьогодні',
		todayStatus: '',
		clearText: 'Очистити',
		clearStatus: '',
		closeText: 'Закрити',
		closeStatus: '',
		yearStatus: '',
		monthStatus: '',
		weekText: 'Не',
		weekStatus: '',
		dayStatus: 'D, M d',
		defaultStatus: '',
		isRTL: false
	};
	$.datepick.setDefaults($.datepick.regionalOptions.uk);
})(jQuery);

/* http://keith-wood.name/datepick.html
   Urdu localisation for jQuery Datepicker.
   Mansoor Munib -- mansoormunib@gmail.com <http://www.mansoor.co.nr/mansoor.html>
   Thanks to Habib Ahmed, ObaidUllah Anwar. */
(function($) {
	'use strict';
	$.datepick.regionalOptions.ur = {
		monthNames: ['جنوری','فروری','مارچ','اپریل','مئی','جون',
		'جولائی','اگست','ستمبر','اکتوبر','نومبر','دسمبر'],
		monthNamesShort: ['1','2','3','4','5','6',
		'7','8','9','10','11','12'],
		dayNames: ['اتوار','پير','منگل','بدھ','جمعرات','جمعہ','ہفتہ'],
		dayNamesShort: ['اتوار','پير','منگل','بدھ','جمعرات','جمعہ','ہفتہ'],
		dayNamesMin: ['اتوار','پير','منگل','بدھ','جمعرات','جمعہ','ہفتہ'],
		dateFormat: 'dd/mm/yyyy',
		firstDay: 0,
		renderer: $.datepick.defaultRenderer,
		prevText: '&#x3c;گذشتہ',
		prevStatus: 'ماه گذشتہ',
		prevJumpText: '&#x3c;&#x3c;',
		prevJumpStatus: 'برس گذشتہ',
		nextText: 'آئندہ&#x3e;',
		nextStatus: 'ماه آئندہ',
		nextJumpText: '&#x3e;&#x3e;',
		nextJumpStatus: 'برس آئندہ',
		currentText: 'رواں',
		currentStatus: 'ماه رواں',
		todayText: 'آج',
		todayStatus: 'آج',
		clearText: 'حذف تاريخ',
		clearStatus: 'کریں حذف تاریخ',
		closeText: 'کریں بند',
		closeStatus: 'کیلئے کرنے بند',
		yearStatus: 'برس تبدیلی',
		monthStatus: 'ماه تبدیلی',
		weekText: 'ہفتہ',
		weekStatus: 'ہفتہ',
		dayStatus: 'انتخاب D, M d',
		defaultStatus: 'کریں منتخب تاريخ',
		isRTL: true
	};
	$.datepick.setDefaults($.datepick.regionalOptions.ur);
})(jQuery);
/* http://keith-wood.name/datepick.html
   Vietnamese localisation for jQuery Datepicker.
   Translated by Le Thanh Huy (lthanhhuy@cit.ctu.edu.vn). */
(function($) {
	'use strict';
	$.datepick.regionalOptions.vi = {
		monthNames: ['Tháng Một', 'Tháng Hai', 'Tháng Ba', 'Tháng Tư', 'Tháng Năm', 'Tháng Sáu',
		'Tháng Bảy', 'Tháng Tám', 'Tháng Chín', 'Tháng Mười', 'Tháng Mười Một', 'Tháng Mười Hai'],
		monthNamesShort: ['Tháng 1', 'Tháng 2', 'Tháng 3', 'Tháng 4', 'Tháng 5', 'Tháng 6',
		'Tháng 7', 'Tháng 8', 'Tháng 9', 'Tháng 10', 'Tháng 11', 'Tháng 12'],
		dayNames: ['Chủ Nhật', 'Thứ Hai', 'Thứ Ba', 'Thứ Tư', 'Thứ Năm', 'Thứ Sáu', 'Thứ Bảy'],
		dayNamesShort: ['CN', 'T2', 'T3', 'T4', 'T5', 'T6', 'T7'],
		dayNamesMin: ['CN', 'T2', 'T3', 'T4', 'T5', 'T6', 'T7'],
		dateFormat: 'dd/mm/yyyy',
		firstDay: 0,
		renderer: $.datepick.defaultRenderer,
		prevText: '&#x3c;Trước',
		prevStatus: 'Tháng trước',
		prevJumpText: '&#x3c;&#x3c;',
		prevJumpStatus: 'Năm trước',
		nextText: 'Tiếp&#x3e;',
		nextStatus: 'Tháng sau',
		nextJumpText: '&#x3e;&#x3e;',
		nextJumpStatus: 'Năm sau',
		currentText: 'Hôm nay',
		currentStatus: 'Tháng hiện tại',
		todayText: 'Hôm nay',
		todayStatus: 'Tháng hiện tại',
		clearText: 'Xóa',
		clearStatus: 'Xóa ngày hiện tại',
		closeText: 'Đóng',
		closeStatus: 'Đóng và không lưu lại thay đổi',
		yearStatus: 'Năm khác',
		monthStatus: 'Tháng khác',
		weekText: 'Tu',
		weekStatus: 'Tuần trong năm',
		dayStatus: 'Đang chọn DD, \'ngày\' d M',
		defaultStatus: 'Chọn ngày',
		isRTL: false
	};
	$.datepick.setDefaults($.datepick.regionalOptions.vi);
})(jQuery);

/* http://keith-wood.name/datepick.html
   Simplified Chinese localisation for jQuery Datepicker.
   Written by Cloudream (cloudream@gmail.com). */
(function($) {
	'use strict';
	$.datepick.regionalOptions['zh-CN'] = {
		monthNames: ['一月','二月','三月','四月','五月','六月',
		'七月','八月','九月','十月','十一月','十二月'],
		monthNamesShort: ['一','二','三','四','五','六',
		'七','八','九','十','十一','十二'],
		dayNames: ['星期日','星期一','星期二','星期三','星期四','星期五','星期六'],
		dayNamesShort: ['周日','周一','周二','周三','周四','周五','周六'],
		dayNamesMin: ['日','一','二','三','四','五','六'],
		dateFormat: 'yyyy-mm-dd',
		firstDay: 1,
		renderer: $.extend({}, $.datepick.defaultRenderer,
			{month: $.datepick.defaultRenderer.month.
				replace(/monthHeader/, 'monthHeader:MM yyyy年')}),
		prevText: '&#x3c;上月',
		prevStatus: '显示上月',
		prevJumpText: '&#x3c;&#x3c;',
		prevJumpStatus: '显示上一年',
		nextText: '下月&#x3e;',
		nextStatus: '显示下月',
		nextJumpText: '&#x3e;&#x3e;',
		nextJumpStatus: '显示下一年',
		currentText: '今天',
		currentStatus: '显示本月',
		todayText: '今天',
		todayStatus: '显示本月',
		clearText: '清除',
		clearStatus: '清除已选日期',
		closeText: '关闭',
		closeStatus: '不改变当前选择',
		yearStatus: '选择年份',
		monthStatus: '选择月份',
		weekText: '周',
		weekStatus: '年内周次',
		dayStatus: '选择 m月 d日, DD',
		defaultStatus: '请选择日期',
		isRTL: false
	};
	$.datepick.setDefaults($.datepick.regionalOptions['zh-CN']);
})(jQuery);

/* http://keith-wood.name/datepick.html
   Hong Kong Chinese localisation for jQuery Datepicker.
   Written by SCCY (samuelcychan@gmail.com). */
(function($) {
	'use strict';
	$.datepick.regionalOptions['zh-HK'] = {
		monthNames: ['一月','二月','三月','四月','五月','六月',
		'七月','八月','九月','十月','十一月','十二月'],
		monthNamesShort: ['一','二','三','四','五','六',
		'七','八','九','十','十一','十二'],
		dayNames: ['星期日','星期一','星期二','星期三','星期四','星期五','星期六'],
		dayNamesShort: ['周日','周一','周二','周三','周四','周五','周六'],
		dayNamesMin: ['日','一','二','三','四','五','六'],
		dateFormat: 'dd-mm-yyyy',
		firstDay: 0,
		renderer: $.extend({}, $.datepick.defaultRenderer,
			{month: $.datepick.defaultRenderer.month.
				replace(/monthHeader/, 'monthHeader:yyyy年 MM')}),
		prevText: '&#x3c;上月',
		prevStatus: '顯示上月',
		prevJumpText: '&#x3c;&#x3c;',
		prevJumpStatus: '顯示上一年',
		nextText: '下月&#x3e;',
		nextStatus: '顯示下月',
		nextJumpText: '&#x3e;&#x3e;',
		nextJumpStatus: '顯示下一年',
		currentText: '今天',
		currentStatus: '顯示本月',
		todayText: '今天',
		todayStatus: '顯示本月',
		clearText: '清除',
		clearStatus: '清除已選日期',
		closeText: '關閉',
		closeStatus: '不改變目前的選擇',
		yearStatus: '選擇年份',
		monthStatus: '選擇月份',
		weekText: '周',
		weekStatus: '年內周次',
		dayStatus: '選擇 m月 d日, DD',
		defaultStatus: '請選擇日期',
		isRTL: false
	};
	$.datepick.setDefaults($.datepick.regionalOptions['zh-HK']);
})(jQuery);

/* http://keith-wood.name/datepick.html
   Traditional Chinese localisation for jQuery Datepicker.
   Written by Ressol (ressol@gmail.com). */
(function($) {
	'use strict';
	$.datepick.regionalOptions['zh-TW'] = {
		monthNames: ['一月','二月','三月','四月','五月','六月',
		'七月','八月','九月','十月','十一月','十二月'],
		monthNamesShort: ['一','二','三','四','五','六',
		'七','八','九','十','十一','十二'],
		dayNames: ['星期日','星期一','星期二','星期三','星期四','星期五','星期六'],
		dayNamesShort: ['周日','周一','周二','周三','周四','周五','周六'],
		dayNamesMin: ['日','一','二','三','四','五','六'],
		dateFormat: 'yyyy/mm/dd',
		firstDay: 1,
		renderer: $.extend({}, $.datepick.defaultRenderer,
			{month: $.datepick.defaultRenderer.month.
				replace(/monthHeader/, 'monthHeader:MM yyyy年')}),
		prevText: '&#x3c;上月',
		prevStatus: '顯示上月',
		prevJumpText: '&#x3c;&#x3c;',
		prevJumpStatus: '顯示上一年',
		nextText: '下月&#x3e;',
		nextStatus: '顯示下月',
		nextJumpText: '&#x3e;&#x3e;',
		nextJumpStatus: '顯示下一年',
		currentText: '今天',
		currentStatus: '顯示本月',
		todayText: '今天',
		todayStatus: '顯示本月',
		clearText: '清除',
		clearStatus: '清除已選日期',
		closeText: '關閉',
		closeStatus: '不改變目前的選擇',
		yearStatus: '選擇年份',
		monthStatus: '選擇月份',
		weekText: '周',
		weekStatus: '年內周次',
		dayStatus: '選擇 m月 d日, DD',
		defaultStatus: '請選擇日期',
		isRTL: false
	};
	$.datepick.setDefaults($.datepick.regionalOptions['zh-TW']);
})(jQuery);
