package com.example.adhani.data

import com.example.adhani.model.DhikrCategory
import com.example.adhani.model.DhikrItem

object AdhkarRepository {

    val allAdhkar: List<DhikrItem> = listOf(
        // MORNING (Sabah)
        DhikrItem(
            id = "m_1",
            category = DhikrCategory.MORNING,
            arabicText = "أَصْبَحْنَا وَأَصْبَحَ الْمُلْكُ لِلَّهِ، وَالْحَمْدُ لِلَّهِ، لاَ إِلَهَ إِلاَّ اللَّهُ وَحْدَهُ لاَ شَرِيكَ لَهُ، لَهُ الْمُلْكُ وَلَهُ الْحَمْدُ وَهُوَ عَلَى كُلِّ شَيْءٍ قَدِيرٌ",
            transliteration = "Asbahna wa-asbahal-mulku lillah, wal-hamdu lillah, la ilaha illallahu wahdahu la shareeka lah, lahul-mulku wa lahul-hamdu wa huwa 'ala kulli shay'in qadeer.",
            translation = "We have entered the morning and the kingdom belongs to Allah, and all praise is due to Allah. None has the right to be worshipped except Allah alone, without partner. To Him belongs all sovereignty and praise, and He has power over all things.",
            virtue = "Comprehensive declaration of divine sovereignty at sunrise.",
            source = "Sahih Muslim 2723",
            targetCount = 1
        ),
        DhikrItem(
            id = "m_2",
            category = DhikrCategory.MORNING,
            arabicText = "اللَّهُمَّ أَنْتَ رَبِّي لاَ إِلَهَ إِلاَّ أَنْتَ، خَلَقْتَنِي وَأَنَا عَبْدُكَ، وَأَنَا عَلَى عَهْدِكَ وَوَعْدِكَ مَا اسْتَطَعْتُ، أَعُوذُ بِكَ مِنْ شَرِّ مَا صَنَعْتُ، أَبُوءُ لَكَ بِنِعْمَتِكَ عَلَيَّ، وَأَبُوءُ بِذَنْبِي فَاغْفِرْ لِي فَإِنَّهُ لاَ يَغْفِرُ الذُّنُوبَ إِلاَّ أَنْتَ",
            transliteration = "Allahumma anta Rabbi la ilaha illa anta, khalaqtani wa ana 'abduka, wa ana 'ala 'ahdika wa wa'dika ma-stata'tu, a'oodhu bika min sharri ma sana'tu, aboo'u laka bini'matika 'alayya, wa aboo'u bidhanbi faghfir li fa-innahu la yaghfirudh-dhunooba illa ant.",
            translation = "O Allah, You are my Lord, there is none worthy of worship except You. You created me and I am Your slave. I abide by Your covenant and promise as best as I can. I seek refuge in You from the evil of what I have done. I acknowledge Your blessing upon me and I acknowledge my sin, so forgive me, for none forgives sins except You.",
            virtue = "Sayyid al-Istighfar (The Master of Supplications for Forgiveness). Whoever says it during the day with firm faith and dies before evening will be from the people of Paradise.",
            source = "Sahih al-Bukhari 6306",
            targetCount = 1
        ),
        DhikrItem(
            id = "m_3",
            category = DhikrCategory.MORNING,
            arabicText = "بِسْمِ اللَّهِ الَّذِي لاَ يَضُرُّ مَعَ اسْمِهِ شَيْءٌ فِي الأَرْضِ وَلاَ فِي السَّمَاءِ وَهُوَ السَّمِيعُ الْعَلِيمُ",
            transliteration = "Bismillahi-lladhi la yadurru ma'as-mihi shay'un fil-ardi wa la fis-sama'i wa huwas-Samee'ul-'Aleem.",
            translation = "In the Name of Allah, with Whose Name nothing can cause harm in the earth nor in the heavens, and He is the All-Hearing, the All-Knowing.",
            virtue = "Whoever recites this three times in the morning and evening, nothing will harm him.",
            source = "Sunan Abi Dawud 5088, At-Tirmidhi",
            targetCount = 3
        ),
        DhikrItem(
            id = "m_4",
            category = DhikrCategory.MORNING,
            arabicText = "سُبْحَانَ اللَّهِ وَبِحَمْدِهِ، عَدَدَ خَلْقِهِ، وَرِضَا نَفْسِهِ، وَزِنَةَ عَرْشِهِ، وَمِدَادَ كَلِمَاتِهِ",
            transliteration = "Subhanallahi wa bihamdih, 'adada khalqihi, wa rida nafsihi, wa zinata 'arshihi, wa midada kalimatih.",
            translation = "Glory be to Allah and all praise is His, according to the count of His creation, according to His satisfaction, according to the weight of His Throne, and according to the ink of His words.",
            virtue = "Heavier on the scale than hours of continuous worship.",
            source = "Sahih Muslim 2726",
            targetCount = 3
        ),

        // EVENING (Masaa)
        DhikrItem(
            id = "e_1",
            category = DhikrCategory.EVENING,
            arabicText = "أَمْسَيْنَا وَأَمْسَى الْمُلْكُ لِلَّهِ، وَالْحَمْدُ لِلَّهِ، لاَ إِلَهَ إِلاَّ اللَّهُ وَحْدَهُ لاَ شَرِيكَ لَهُ، لَهُ الْمُلْكُ وَلَهُ الْحَمْدُ وَهُوَ عَلَى كُلِّ شَيْءٍ قَدِيرٌ",
            transliteration = "Amsayna wa-amsal-mulku lillah, wal-hamdu lillah, la ilaha illallahu wahdahu la shareeka lah, lahul-mulku wa lahul-hamdu wa huwa 'ala kulli shay'in qadeer.",
            translation = "We have entered the evening and the kingdom belongs to Allah, and all praise is due to Allah. None has the right to be worshipped except Allah alone, without partner.",
            virtue = "Guards the soul during the twilight hours.",
            source = "Sahih Muslim 2723",
            targetCount = 1
        ),
        DhikrItem(
            id = "e_2",
            category = DhikrCategory.EVENING,
            arabicText = "أَعُوذُ بِكَلِمَاتِ اللَّهِ التَّامَّاتِ مِنْ شَرِّ مَا خَلَقَ",
            transliteration = "A'oodhu bikalimatillahit-tammati min sharri ma khalaq.",
            translation = "I seek refuge in the perfect words of Allah from the evil of what He has created.",
            virtue = "Whoever says this three times in the evening will not be harmed by any poison or pestilence that night.",
            source = "Sahih Muslim 2709",
            targetCount = 3
        ),

        // POST-PRAYER (Ba'd al-Salat)
        DhikrItem(
            id = "p_1",
            category = DhikrCategory.POST_PRAYER,
            arabicText = "أَسْتَغْفِرُ اللَّهَ، أَسْتَغْفِرُ اللَّهَ، أَسْتَغْفِرُ اللَّهَ. اللَّهُمَّ أَنْتَ السَّلاَمُ وَمِنْكَ السَّلاَمُ، تَبَارَكْتَ يَا ذَا الْجَلاَلِ وَالإِكْرَامِ",
            transliteration = "Astaghfirullah (3x). Allahumma antas-Salamu wa minkas-salam, tabarakta ya dhal-Jalali wal-Ikram.",
            translation = "I seek Allah's forgiveness (3 times). O Allah, You are Peace and from You comes peace. Blessed are You, O Owner of Majesty and Honor.",
            virtue = "The direct Sunnah immediately following the final Tasleem of obligatory prayer.",
            source = "Sahih Muslim 591",
            targetCount = 1
        ),
        DhikrItem(
            id = "p_2",
            category = DhikrCategory.POST_PRAYER,
            arabicText = "سُبْحَانَ اللَّهِ (٣٣)، الْحَمْدُ لِلَّهِ (٣٣)، اللَّهُ أَكْبَرُ (٣٣)",
            transliteration = "SubhanAllah (33x), Alhamdulillah (33x), Allahu Akbar (33x)",
            translation = "Glory be to Allah (33 times), Praise be to Allah (33 times), Allah is the Greatest (33 times).",
            virtue = "Whoever completes this with 'La ilaha illallah wahdahu la shareeka lah...' will have his sins forgiven even if they were like the foam of the sea.",
            source = "Sahih Muslim 597",
            targetCount = 33
        ),
        DhikrItem(
            id = "p_3",
            category = DhikrCategory.POST_PRAYER,
            arabicText = "اللَّهُ لاَ إِلَهَ إِلاَّ هُوَ الْحَيُّ الْقَيُّومُ لاَ تَأْخُذُهُ سِنَةٌ وَلاَ نَوْمٌ لَّهُ مَا فِي السَّمَاوَاتِ وَمَا فِي الأَرْضِ",
            transliteration = "Allahu la ilaha illa huwal-Hayyul-Qayyum, la ta'khudhuhu sinatuw-wa la nawm...",
            translation = "Ayat al-Kursi (The Verse of the Throne). Allah! There is no deity except Him, the Ever-Living, the Sustainer of all existence.",
            virtue = "Whoever recites it after every obligatory prayer, nothing prevents him from entering Paradise except death.",
            source = "Sunan an-Nasa'i, Sahih al-Jami' 6464",
            targetCount = 1
        ),

        // SLEEP (Nawm)
        DhikrItem(
            id = "s_1",
            category = DhikrCategory.SLEEP,
            arabicText = "بِاسْمِكَ رَبِّي وَضَعْتُ جَنْبِي، وَبِكَ أَرْفَعُهُ، فَإِنْ أَمْسَكْتَ نَفْسِي فَارْحَمْهَا، وَإِنْ أَرْسَلْتَهَا فَاحْفَظْهَا بِمَا تَحْفَظُ بِهِ عِبَادَكَ الصَّالِحِينَ",
            transliteration = "Bismika Rabbi wada'tu janbi, wa bika arfa'uh, fa in amsakta nafsi far-hamha, wa in arsaltaha fah-fadh-ha bima tahfadhu bihi 'ibadakas-saliheen.",
            translation = "In Your Name, my Lord, I lay down my side and in Your Name I raise it. If You take my soul, have mercy on it, and if You send it back, protect it as You protect Your righteous slaves.",
            virtue = "Invokes angelic guardianship over the resting soul.",
            source = "Sahih al-Bukhari 6320",
            targetCount = 1
        ),

        // TRAVEL (Safar)
        DhikrItem(
            id = "t_1",
            category = DhikrCategory.TRAVEL,
            arabicText = "سُبْحَانَ الَّذِي سَخَّرَ لَنَا هَذَا وَمَا كُنَّا لَهُ مُقْرِنِينَ وَإِنَّا إِلَى رَبِّنَا لَمُنْقَلِبُونَ",
            transliteration = "Subhanalladhi sakh-khara lana hadha wa ma kunna lahu muqrineen, wa inna ila Rabbina lamunqaliboon.",
            translation = "Glory to Him who has subjected this to us, and we could never have had it by our efforts. And verily, to our Lord we shall return.",
            virtue = "Protects the traveller on all conveyances and journeys.",
            source = "Surah Az-Zukhruf 43:13-14",
            targetCount = 1
        ),

        // DAILY REMEMBRANCE (Yawmiyyah)
        DhikrItem(
            id = "d_1",
            category = DhikrCategory.DAILY,
            arabicText = "لَا حَوْلَ وَلَا قُوَّةَ إِلَّا بِاللَّهِ الْعَلِيِّ الْعَظِيمِ",
            transliteration = "La hawla wa la quwwata illa billahil-'Aliyyil-'Adheem.",
            translation = "There is no power and no strength except with Allah, the Most High, the Supreme.",
            virtue = "A treasure from beneath the Throne of the Most Merciful.",
            source = "Sahih al-Bukhari 6409",
            targetCount = 100
        ),
        DhikrItem(
            id = "d_2",
            category = DhikrCategory.DAILY,
            arabicText = "سُبْحَانَ اللَّهِ وَبِحَمْدِهِ، سُبْحَانَ اللَّهِ الْعَظِيمِ",
            transliteration = "Subhanallahi wa bihamdih, Subhanallahil-'Adheem.",
            translation = "Glory be to Allah and His is the praise, Glory be to Allah the Tremendous.",
            virtue = "Two phrases light on the tongue, heavy on the scales, beloved to the Most Merciful.",
            source = "Sahih al-Bukhari 6682",
            targetCount = 100
        )
    )

    fun getByCategory(category: DhikrCategory): List<DhikrItem> {
        return allAdhkar.filter { it.category == category }
    }
}
