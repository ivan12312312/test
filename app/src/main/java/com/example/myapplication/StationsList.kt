package com.example.myapplication

data class Station(
    val title: String,
    val uri: String,
)

val StationsList = arrayOf(
    Station("Record","https://radiorecord.hostingradio.ru/rr_main96.aacp"),
    Station("Russian Mix","https://radiorecord.hostingradio.ru/rus96.aacp"),
    Station("Супердискотека 90-х","https://radiorecord.hostingradio.ru/sd9096.aacp"),
    Station("Russian Hits","https://radiorecord.hostingradio.ru/russianhits96.aacp"),
    Station("Chill-Out","https://radiorecord.hostingradio.ru/chil96.aacp"),
    Station("Deep","https://radiorecord.hostingradio.ru/deep96.aacp"),
    Station("Pirate Station","https://radiorecord.hostingradio.ru/ps96.aacp"),
    Station("Маятник Фуко","https://radiorecord.hostingradio.ru/mf96.aacp"),
    Station("Рекорд 00-х","https://radiorecord.hostingradio.ru/200096.aacp"),
    Station("Megamix","https://radiorecord.hostingradio.ru/mix96.aacp"),
    Station("Trancemission","https://radiorecord.hostingradio.ru/tm96.aacp"),
    Station("Chill House","https://radiorecord.hostingradio.ru/chillhouse96.aacp"),
    Station("Big Hits","https://radiorecord.hostingradio.ru/bighits96.aacp"),
    Station("Record Gold","https://radiorecord.hostingradio.ru/gold96.aacp"),
    Station("Summer Lounge","https://radiorecord.hostingradio.ru/summerlounge96.aacp"),
    Station("Summer Dance","https://radiorecord.hostingradio.ru/summerparty96.aacp"),
    Station("Remix","https://radiorecord.hostingradio.ru/rmx96.aacp"),
    Station("На Хайпе","https://radiorecord.hostingradio.ru/hype96.aacp"),
    Station("Russian Gold","https://radiorecord.hostingradio.ru/russiangold96.aacp"),
    Station("Bass House","https://radiorecord.hostingradio.ru/jackin96.aacp"),
    Station("10's Dance","https://radiorecord.hostingradio.ru/201096.aacp"),
    Station("VIP House","https://radiorecord.hostingradio.ru/vip96.aacp"),
    Station("Party 24/7","https://radiorecord.hostingradio.ru/party96.aacp"),
    Station("Trance Classics","https://radiorecord.hostingradio.ru/trancehits96.aacp"),
    Station("Innocence","https://radiorecord.hostingradio.ru/ibiza96.aacp"),
    Station("House Hits","https://radiorecord.hostingradio.ru/househits96.aacp"),
    Station("EDM","https://radiorecord.hostingradio.ru/club96.aacp"),
    Station("Minimal/Tech","https://radiorecord.hostingradio.ru/mini96.aacp"),
    Station("TOP 100 EDM","https://radiorecord.hostingradio.ru/top100edm96.aacp"),
    Station("Tropical","https://radiorecord.hostingradio.ru/trop96.aacp"),
    Station("Phonk","https://radiorecord.hostingradio.ru/phonk96.aacp"),
    Station("Workout","https://radiorecord.hostingradio.ru/workout32.aacp"),
    Station("House Classics","https://radiorecord.hostingradio.ru/houseclss96.aacp"),
    Station("D'n'B Classics","https://radiorecord.hostingradio.ru/drumhits96.aacp"),
    Station("EDM Classics","https://radiorecord.hostingradio.ru/edmhits96.aacp"),
    Station("Lo-Fi","https://radiorecord.hostingradio.ru/lofi96.aacp"),
    Station("Future House","https://radiorecord.hostingradio.ru/fut96.aacp"),
    Station("Organic","https://radiorecord.hostingradio.ru/organic96.aacp"),
    Station("Trap","https://radiorecord.hostingradio.ru/trap96.aacp"),
    Station("Future Rave","https://radiorecord.hostingradio.ru/futurerave96.aacp"),
    Station("Tech House","https://radiorecord.hostingradio.ru/techouse96.aacp"),
    Station("Progressive","https://radiorecord.hostingradio.ru/progr96.aacp"),
    Station("Rap Classics","https://radiorecord.hostingradio.ru/rapclassics96.aacp"),
    Station("Breaks","https://radiorecord.hostingradio.ru/brks96.aacp"),
    Station("Trancehouse","https://radiorecord.hostingradio.ru/trancehouse96.aacp"),
    Station("Rap Hits","https://radiorecord.hostingradio.ru/rap96.aacp"),
    Station("GOA/PSY","https://radiorecord.hostingradio.ru/goa96.aacp"),
    Station("Dream Dance","https://radiorecord.hostingradio.ru/dream96.aacp"),
    Station("Black Rap","https://radiorecord.hostingradio.ru/yo96.aacp"),
    Station("Dream Pop","https://radiorecord.hostingradio.ru/dreampop96.aacp"),
    Station("Electro","https://radiorecord.hostingradio.ru/elect96.aacp"),
    Station("Uplifting","https://radiorecord.hostingradio.ru/uplift96.aacp"),
    Station("Future Bass","https://radiorecord.hostingradio.ru/fbass96.aacp"),
    Station("Ambient","https://radiorecord.hostingradio.ru/ambient96.aacp"),
    Station("Neurofunk","https://radiorecord.hostingradio.ru/neurofunk96.aacp"),
    Station("Dancecore","https://radiorecord.hostingradio.ru/dc96.aacp"),
    Station("Liquid Funk","https://radiorecord.hostingradio.ru/liquidfunk96.aacp"),
    Station("Reggae","https://radiorecord.hostingradio.ru/reggae32.aacp"),
    Station("Latina Dance","https://radiorecord.hostingradio.ru/latina96.aacp"),
    Station("Eurodance","https://radiorecord.hostingradio.ru/eurodance96.aacp"),
    Station("Dubstep","https://radiorecord.hostingradio.ru/dub96.aacp"),
    Station("Technopop","https://radiorecord.hostingradio.ru/technopop96.aacp"),
    Station("Techno","https://radiorecord.hostingradio.ru/techno96.aacp"),
    Station("Disco/Funk","https://radiorecord.hostingradio.ru/discofunk96.aacp"),
    Station("UK Garage","https://radiorecord.hostingradio.ru/ukgarage96.aacp"),
    Station("Hardstyle","https://radiorecord.hostingradio.ru/teo96.aacp"),
    Station("Tecktonik","https://radiorecord.hostingradio.ru/tecktonik96.aacp"),
    Station("Live DJ-sets","https://radiorecord.hostingradio.ru/livedjsets96.aacp"),
    Station("Midtempo","https://radiorecord.hostingradio.ru/mt96.aacp"),
    Station("Synthwave","https://radiorecord.hostingradio.ru/synth96.aacp"),
    Station("Old School","https://radiorecord.hostingradio.ru/pump96.aacp"),
    Station("Hard Bass","https://radiorecord.hostingradio.ru/hbass96.aacp"),
    Station("Darkside","https://radiorecord.hostingradio.ru/darkside96.aacp"),
    Station("Hypnotic","https://radiorecord.hostingradio.ru/hypno96.aacp"),
    Station("Moombahton","https://radiorecord.hostingradio.ru/mmbt96.aacp"),
    Station("2-step","https://radiorecord.hostingradio.ru/2step96.aacp"),
    Station("Groove/Tribal","https://radiorecord.hostingradio.ru/groovetribal96.aacp"),
    Station("Rave FM","https://radiorecord.hostingradio.ru/rave96.aacp"),
    Station("Christmas Chill","https://radiorecord.hostingradio.ru/christmaschill96.aacp"),
    Station("Jungle","https://radiorecord.hostingradio.ru/jungle96.aacp"),
    Station("Complextro","https://radiorecord.hostingradio.ru/complextro96.aacp"),
    Station("Гоп FM","https://radiorecord.hostingradio.ru/gop96.aacp"),
    Station("Rock","https://radiorecord.hostingradio.ru/rock96.aacp"),
    Station("60's Dance","https://radiorecord.hostingradio.ru/cadillac96.aacp"),
    Station("70's Dance","https://radiorecord.hostingradio.ru/197096.aacp"),
    Station("Record 80-х","https://radiorecord.hostingradio.ru/198096.aacp"),
    Station("Руки Вверх!","https://radiorecord.hostingradio.ru/rv96.aacp"),
    Station("Веснушка FM","https://radiorecord.hostingradio.ru/deti96.aacp"),
    Station("Медляк FM","https://radiorecord.hostingradio.ru/mdl96.aacp"),
    Station("Нафталин FM","https://radiorecord.hostingradio.ru/naft96.aacp"),
    Station("Гастарбайтер FM","https://radiorecord.hostingradio.ru/gast96.aacp"),
    Station("Симфония FM","https://radiorecord.hostingradio.ru/symph96.aacp"),
    Station("Christmas","https://radiorecord.hostingradio.ru/christmas96.aacp")
)