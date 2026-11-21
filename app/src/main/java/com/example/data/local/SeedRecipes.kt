package com.example.data.local

import com.squareup.moshi.Moshi
import com.squareup.moshi.Types

object SeedRecipes {

    private val moshi = Moshi.Builder().build()
    private val ingredientListType = Types.newParameterizedType(List::class.java, RecipeIngredient::class.java)
    private val ingredientAdapter = moshi.adapter<List<RecipeIngredient>>(ingredientListType)

    fun serializeIngredients(list: List<RecipeIngredient>): String = ingredientAdapter.toJson(list)

    fun parseIngredients(json: String): List<RecipeIngredient> =
        runCatching { ingredientAdapter.fromJson(json) ?: emptyList() }.getOrDefault(emptyList())

    private fun ing(name: String, nameSi: String, qty: String, unit: String) =
        RecipeIngredient(name = name, nameSi = nameSi, quantity = qty, unit = unit)

    fun getPreloadedRecipes(): List<RecipeEntity> = listOf(
        RecipeEntity(
            name = "Parippu (Red Lentil Curry)",
            nameSi = "පරිප්පු කරි",
            ingredientsJson = serializeIngredients(listOf(
                ing("Red Lentils (Dhal)", "පරිප්පු", "250", "g"),
                ing("Coconut Milk", "පොල් කිරි", "1", "cup"),
                ing("Red Onion", "රතු ලූනු", "4", "pcs"),
                ing("Garlic", "සුදු ලූනු", "3", "cloves"),
                ing("Green Chili", "අමු මිරිස්", "2", "pcs"),
                ing("Curry Leaves", "කරපිංචා", "1", "sprig"),
                ing("Turmeric Powder", "කහ කුඩු", "0.5", "tsp"),
                ing("Mustard Seeds", "අබ", "0.5", "tsp")
            )),
            instructions = "1. Wash red lentils thoroughly and place in a clay pot.\n2. Add sliced onion, garlic, green chili, curry leaves, turmeric, and water to cover.\n3. Simmer 12 mins until soft.\n4. Pour thick coconut milk and salt, cook 4 mins.\n5. Temper with mustard seeds, dried chili, and curry leaves in coconut oil.",
            instructionsSi = "1. පරිප්පු හොඳින් සෝදා වළඳකට දමන්න.\n2. කපාගත් ලූනු, සුදු ලූනු, අමු මිරිස්, කරපිංචා, කහ සහ වතුර එකතු කර මිනිත්තු 12ක් තම්බා ගන්න.\n3. උකු පොල් කිරි සහ ලුණු එකතු කර මිනිත්තු 4ක් පිසගන්න.\n4. අබ, වියළි මිරිස් සහ කරපිංචා පොල් තෙලෙන් තෙම්පරාදු කර එකතු කරන්න.",
            prepTime = 20,
            category = "Curry",
            servings = 4,
            isFavorite = true
        ),
        RecipeEntity(
            name = "Pol Sambol",
            nameSi = "පොල් සම්බෝල",
            ingredientsJson = serializeIngredients(listOf(
                ing("Grated Coconut", "ගාගත් පොල්", "2", "cups"),
                ing("Red Onion", "රතු ලූනු", "6", "pcs"),
                ing("Chili Flakes", "කෑලි මිරිස්", "2", "tbsp"),
                ing("Maldive Fish", "උම්බලකඩ", "1", "tbsp"),
                ing("Lime", "දෙහි", "1", "pcs")
            )),
            instructions = "1. Grind chili flakes, red onion, Maldive fish, and salt together.\n2. Mix in freshly grated coconut thoroughly.\n3. Squeeze fresh lime juice and adjust salt to taste.",
            instructionsSi = "1. කෑලි මිරිස්, රතු ලූනු, උම්බලකඩ සහ ලුණු හොඳින් අඹරා ගන්න.\n2. අලුතින් ගාගත් පොල් එකතු කර හොඳින් කලවම් කරන්න.\n3. දෙහි යුෂ මිරිකා ලුණු රස බලා පිළිගන්වන්න.",
            prepTime = 10,
            category = "Side Dish",
            servings = 4,
            isFavorite = true
        ),
        RecipeEntity(
            name = "Sri Lankan Chicken Curry",
            nameSi = "කුකුළු මස් කරි",
            ingredientsJson = serializeIngredients(listOf(
                ing("Chicken", "කුකුළු මස්", "500", "g"),
                ing("Roasted Curry Powder", "බැදපු තුනපහ", "2", "tbsp"),
                ing("Chili Powder", "මිරිස් කුඩු", "1.5", "tbsp"),
                ing("Coconut Milk", "පොල් කිරි", "1", "cup"),
                ing("Onion", "ලූනු", "1", "large"),
                ing("Garlic", "සුදු ලූනු", "5", "cloves"),
                ing("Ginger", "ඉඟුරු", "1", "inch"),
                ing("Curry Leaves", "කරපිංචා", "2", "sprigs")
            )),
            instructions = "1. Marinate chicken with roasted curry powder, chili powder, turmeric, salt, and lime for 20 mins.\n2. Saute onion, garlic, ginger, pandan, and curry leaves.\n3. Add marinated chicken and sear 5 mins.\n4. Pour thin coconut milk, cover and simmer 20 mins.\n5. Finish with thick coconut milk and simmer until rich.",
            instructionsSi = "1. කුකුළු මස් බැදපු තුනපහ, මිරිස් කුඩු, කහ, ලුණු සහ දෙහි අතගා මිනිත්තු 20ක් තබන්න.\n2. ලූනු, සුදු ලූනු, ඉඟුරු, රම්පෙ සහ කරපිංචා තෙලේ බැදගන්න.\n3. කුකුළු මස් දමා මිනිත්තු 5ක් පෙරළන්න.\n4. දිය කිරි දමා මිනිත්තු 20ක් මද ගින්නේ පිස, මිටි කිරි දමා හින්දා ගන්න.",
            prepTime = 40,
            category = "Curry",
            servings = 4,
            isFavorite = true
        ),
        RecipeEntity(
            name = "Ala Thel Dala (Tempered Potato)",
            nameSi = "අල තෙල් දැමීම",
            ingredientsJson = serializeIngredients(listOf(
                ing("Potato", "අල", "500", "g"),
                ing("Onion", "ලූනු", "1", "large"),
                ing("Chili Flakes", "කෑලි මිරිස්", "1.5", "tbsp"),
                ing("Mustard Seeds", "අබ", "0.5", "tsp"),
                ing("Curry Leaves", "කරපිංචා", "1", "sprig"),
                ing("Turmeric Powder", "කහ කුඩු", "0.5", "tsp"),
                ing("Coconut Oil", "පොල් තෙල්", "2", "tbsp")
            )),
            instructions = "1. Boil potatoes, peel, and cube.\n2. Heat coconut oil and pop mustard seeds.\n3. Fry onion, curry leaves, turmeric, and chili flakes.\n4. Toss boiled potato cubes gently until coated and slightly crisp.",
            instructionsSi = "1. අල තම්බා පොතු ඇර කෑලි කපාගන්න.\n2. පොල් තෙල් රත් කර අබ පුපුරවා ගන්න.\n3. ලූනු, කරපිංචා, කහ සහ කෑලි මිරිස් දමා බැදගන්න.\n4. අල කෑලි සහ ලුණු එකතු කර හොඳින් කලවම් කරන්න.",
            prepTime = 25,
            category = "Side Dish",
            servings = 4
        ),
        RecipeEntity(
            name = "Kiribath & Lunu Miris",
            nameSi = "කිරිබත් සහ ලුණු මිරිස්",
            ingredientsJson = serializeIngredients(listOf(
                ing("Red Rice", "රතු කැකුළු සහල්", "2", "cups"),
                ing("Coconut Milk", "පොල් කිරි", "2", "cups"),
                ing("Red Onion", "රතු ලූනු", "8", "pcs"),
                ing("Chili Flakes", "කෑලි මිරිස්", "2", "tbsp"),
                ing("Lime", "දෙහි", "1", "pcs")
            )),
            instructions = "1. Boil rice with 3.5 cups water until soft.\n2. Stir in thick coconut milk and salt, cook on low heat until creamy.\n3. Press onto a flat plate and slice into diamonds.\n4. Serve with pounded red onion, chili flakes, salt, and lime Lunu Miris.",
            instructionsSi = "1. සහල් වතුර කෝප්ප 3.5ක් දමා මෘදු වන තුරු පිසගන්න.\n2. උකු පොල් කිරි සහ ලුණු එකතු කර මද ගින්නේ කිරි හිඳෙන තුරු පිසගන්න.\n3. තැටියක තුනී කර කැට කපා, රතු ලූනු ලුණු මිරිස් සමඟ පිළිගන්වන්න.",
            prepTime = 35,
            category = "Breakfast",
            servings = 5,
            isFavorite = true
        ),
        RecipeEntity(
            name = "Pol Roti (Coconut Flatbread)",
            nameSi = "පොල් රොටී",
            ingredientsJson = serializeIngredients(listOf(
                ing("Wheat Flour", "පාන් පිටි", "2", "cups"),
                ing("Grated Coconut", "ගාගත් පොල්", "1.5", "cups"),
                ing("Red Onion", "රතු ලූනු", "3", "pcs"),
                ing("Green Chili", "අමු මිරිස්", "2", "pcs"),
                ing("Curry Leaves", "කරපිංචා", "1", "sprig")
            )),
            instructions = "1. Combine flour, grated coconut, finely chopped onion, chili, curry leaves, and salt.\n2. Add warm water gradually and knead a soft dough.\n3. Flatten balls onto a greased skillet and cook 3 mins per side until golden.",
            instructionsSi = "1. පාන් පිටි, ගාගත් පොල්, සිහින්ව කපාගත් ලූනු, අමු මිරිස්, කරපිංචා සහ ලුණු මිශ්‍ර කරන්න.\n2. මද උණුසුම් වතුර එකතු කර මෘදු පිටි ගුලියක් අනාගන්න.\n3. තුනී කර රත් වූ තැටියක දෙපැත්ත රන්වන් පැහැ වන තුරු පුළුස්සා ගන්න.",
            prepTime = 25,
            category = "Breakfast",
            servings = 4
        ),
        RecipeEntity(
            name = "Gotukola Mallum",
            nameSi = "ගොටුකොළ සම්බෝල / මැල්ලුම",
            ingredientsJson = serializeIngredients(listOf(
                ing("Gotukola Leaves", "ගොටුකොළ", "1", "bunch"),
                ing("Grated Coconut", "ගාගත් පොල්", "0.75", "cup"),
                ing("Red Onion", "රතු ලූනු", "4", "pcs"),
                ing("Green Chili", "අමු මිරිස්", "2", "pcs"),
                ing("Lime", "දෙහි", "0.5", "pcs")
            )),
            instructions = "1. Wash and finely shred gotukola leaves.\n2. Toss with sliced red onions, green chili, grated coconut, and salt.\n3. Squeeze lime juice right before serving (or lightly warm 1 minute for Mallum).",
            instructionsSi = "1. ගොටුකොළ හොඳින් සෝදා ඉතා සිහින්ව ලියාගන්න.\n2. කපාගත් රතු ලූනු, අමු මිරිස්, ගාගත් පොල් සහ ලුණු මිශ්‍ර කරන්න.\n3. පිළිගැන්වීමට පෙර දෙහි යුෂ මිරිකන්න.",
            prepTime = 12,
            category = "Greens",
            servings = 4
        ),
        RecipeEntity(
            name = "Egg Curry (Biththara Curry)",
            nameSi = "බිත්තර කරි",
            ingredientsJson = serializeIngredients(listOf(
                ing("Eggs", "බිත්තර", "6", "pcs"),
                ing("Coconut Milk", "පොල් කිරි", "1", "cup"),
                ing("Onion", "ලූනු", "1", "medium"),
                ing("Tomato", "තක්කාලි", "1", "pcs"),
                ing("Roasted Curry Powder", "බැදපු තුනපහ", "1.5", "tbsp"),
                ing("Chili Powder", "මිරිස් කුඩු", "1", "tbsp"),
                ing("Curry Leaves", "කරපිංචා", "1", "sprig")
            )),
            instructions = "1. Hard-boil eggs, peel, and lightly score.\n2. Fry onions, garlic, curry leaves, spices, and tomato in oil.\n3. Add coconut milk and simmer until thickened.\n4. Gently add boiled eggs and simmer 5 more minutes.",
            instructionsSi = "1. බිත්තර තම්බා පොතු ඇර සිහින්ව ඉරි ගසන්න.\n2. ලූනු, සුදු ලූනු, කරපිංචා, තුනපහ, මිරිස් කුඩු සහ තක්කාලි තෙලේ බැදගන්න.\n3. පොල් කිරි දමා උතුරවා බිත්තර එකතු කර මිනිත්තු 5ක් පිසගන්න.",
            prepTime = 25,
            category = "Curry",
            servings = 4
        ),
        RecipeEntity(
            name = "Fish Ambul Thiyal",
            nameSi = "මාළු ඇඹුල් තියල්",
            ingredientsJson = serializeIngredients(listOf(
                ing("Tuna Fish", "කෙලවල්ලා මාළු", "500", "g"),
                ing("Goraka (Garcinia)", "ගොරකා", "4", "pcs"),
                ing("Black Pepper", "ගම්මිරිස්", "2", "tbsp"),
                ing("Garlic", "සුදු ලූනු", "4", "cloves"),
                ing("Curry Leaves", "කරපිංචා", "2", "sprigs")
            )),
            instructions = "1. Soak goraka in warm water and grind into a thick paste with black pepper, garlic, and salt.\n2. Coat cubed fish generously with the paste.\n3. Line a clay pot with curry leaves, place fish, add half cup water, and slow-cook until dry.",
            instructionsSi = "1. ගොරකා උණු වතුරේ පොඟවා ගම්මිරිස්, සුදු ලූනු සහ ලුණු සමඟ අඹරා ගන්න.\n2. මාළු කැබලි වල මෙම මිශ්‍රණය හොඳින් අතගාන්න.\n3. වළඳේ අඩියට කරපිංචා අතුරා මාළු තබා වතුර ස්වල්පයක් දමා හොඳින් හිඳෙන තුරු පිසගන්න.",
            prepTime = 45,
            category = "Curry",
            servings = 5
        ),
        RecipeEntity(
            name = "Wambatu Moju (Eggplant Pickle)",
            nameSi = "වම්බටු මෝජු",
            ingredientsJson = serializeIngredients(listOf(
                ing("Eggplant (Brinjal)", "වම්බටු", "400", "g"),
                ing("Red Onion", "රතු ලූනු", "8", "pcs"),
                ing("Green Chili", "අමු මිරිස්", "4", "pcs"),
                ing("Vinegar", "විනාකිරි", "3", "tbsp"),
                ing("Sugar", "සීනි", "2", "tbsp"),
                ing("Mustard Seeds", "අබ", "1", "tsp")
            )),
            instructions = "1. Cut eggplant into thin strips, dust with turmeric, and deep fry until golden.\n2. Simmer vinegar, sugar, ground mustard, chili powder, and salt.\n3. Fold in fried eggplant, sliced red onions, and green chilies.",
            instructionsSi = "1. වම්බටු සිහින් තීරු කපා කහ අතගා රන්වන් පැහැ වන තුරු ගැඹුරු තෙලේ බැදගන්න.\n2. විනාකිරි, සීනි, අඹරාගත් අබ, මිරිස් කුඩු සහ ලුණු උතුරවා ගන්න.\n3. බැදගත් වම්බටු, රතු ලූනු සහ අමු මිරිස් දමා කලවම් කරන්න.",
            prepTime = 35,
            category = "Side Dish",
            servings = 6
        ),
        RecipeEntity(
            name = "Chicken Kottu Roti",
            nameSi = "චිකන් කොත්තු",
            ingredientsJson = serializeIngredients(listOf(
                ing("Godamba Roti", "ගෝදම්බා රොටී", "4", "pcs"),
                ing("Chicken", "කුකුළු මස්", "250", "g"),
                ing("Eggs", "බිත්තර", "2", "pcs"),
                ing("Carrot", "කැරට්", "1", "medium"),
                ing("Leeks", "ලීක්ස්", "1", "stalk"),
                ing("Onion", "ලූනු", "1", "medium")
            )),
            instructions = "1. Chop roti into bite-size strips.\n2. Stir-fry onions, curry leaves, grated carrot, and sliced leeks.\n3. Scramble eggs in the pan, then add chopped roti and chicken curry gravy.\n4. Chop and toss vigorously on high heat until smoky and blended.",
            instructionsSi = "1. රොටී කුඩා කෑලි වලට කපාගන්න.\n2. ලූනු, කරපිංචා, කැරට් සහ ලීක්ස් තෙලේ බැදගන්න.\n3. බිත්තර කඩා දමා කලවම් කර, රොටී කැබලි සහ කුකුළු මස් හොදි එකතු කර හොඳින් කොටා ගන්න.",
            prepTime = 25,
            category = "Dinner",
            servings = 3
        ),
        RecipeEntity(
            name = "Pumpkin Curry (Wattakka)",
            nameSi = "වට්ටක්කා කරි",
            ingredientsJson = serializeIngredients(listOf(
                ing("Pumpkin", "වට්ටක්කා", "400", "g"),
                ing("Coconut Milk", "පොල් කිරි", "1", "cup"),
                ing("Mustard Seeds", "අබ", "0.5", "tsp"),
                ing("Curry Powder", "තුනපහ", "1", "tsp"),
                ing("Turmeric Powder", "කහ කුඩු", "0.5", "tsp"),
                ing("Onion", "ලූනු", "1", "small")
            )),
            instructions = "1. Cube pumpkin with skin on if tender.\n2. Cook with onion, garlic, curry powder, turmeric, and thin coconut milk until soft.\n3. Add thick coconut milk and mustard paste, simmer 5 minutes.",
            instructionsSi = "1. වට්ටක්කා කැබලි කපා ලූනු, සුදු ලූනු, තුනපහ, කහ සහ දිය කිරි දමා තම්බා ගන්න.\n2. මිටි කිරි සහ අඹරාගත් අබ එකතු කර මිනිත්තු 5ක් පිසගන්න.",
            prepTime = 25,
            category = "Curry",
            servings = 4
        ),
        RecipeEntity(
            name = "Green Beans Curry (Bonchi)",
            nameSi = "බෝංචි කිරට",
            ingredientsJson = serializeIngredients(listOf(
                ing("Green Beans", "බෝංචි", "250", "g"),
                ing("Coconut Milk", "පොල් කිරි", "1", "cup"),
                ing("Onion", "ලූනු", "1", "small"),
                ing("Garlic", "සුදු ලූනු", "2", "cloves"),
                ing("Turmeric Powder", "කහ කුඩු", "0.5", "tsp"),
                ing("Green Chili", "අමු මිරිස්", "2", "pcs")
            )),
            instructions = "1. Trim and diagonally slice green beans.\n2. Place in pot with onion, garlic, green chili, curry leaves, turmeric, and thin milk.\n3. Simmer uncovered 8 mins to keep vibrant green, then add thick coconut milk.",
            instructionsSi = "1. බෝංචි කපා ලූනු, සුදු ලූනු, අමු මිරිස්, කරපිංචා, කහ සහ දිය කිරි දමා පිසගන්න.\n2. කොළ පැහැය රැකෙන සේ පියන නොවසා මිටි කිරි දමා උතුරවා ගන්න.",
            prepTime = 18,
            category = "Curry",
            servings = 4
        ),
        RecipeEntity(
            name = "String Hoppers & Kiri Hodi",
            nameSi = "ඉඳිආප්ප සහ කිරි හොදි",
            ingredientsJson = serializeIngredients(listOf(
                ing("Red Rice Flour", "රතු සහල් පිටි", "2", "cups"),
                ing("Coconut Milk", "පොල් කිරි", "1.5", "cups"),
                ing("Onion", "ලූනු", "1", "small"),
                ing("Turmeric Powder", "කහ කුඩු", "0.5", "tsp"),
                ing("Fenugreek Seeds", "උළුහාල්", "0.5", "tsp"),
                ing("Lime", "දෙහි", "0.5", "pcs")
            )),
            instructions = "1. Mix roasted rice flour with hot water and salt, press onto mats, and steam 5 mins.\n2. For Kiri Hodi, gently simmer coconut milk with onion, fenugreek, turmeric, curry leaves, and finish off-heat with lime.",
            instructionsSi = "1. බැදපු සහල් පිටි උණු වතුරෙන් අනා ඉඳිආප්ප වංගෙඩියෙන් වත්කර හුමාලයෙන් තම්බා ගන්න.\n2. කිරි හොදි සඳහා පොල් කිරි, ලූනු, උළුහාල්, කහ සහ කරපිංචා මද ගින්නේ හැඳිගාමින් පිස අවසානයේ දෙහි එක් කරන්න.",
            prepTime = 35,
            category = "Breakfast",
            servings = 4
        ),
        RecipeEntity(
            name = "Tempered Chickpeas (Kadala)",
            nameSi = "කඩල තෙම්පරාදුව",
            ingredientsJson = serializeIngredients(listOf(
                ing("Chickpeas (Kadala)", "කඩල", "250", "g"),
                ing("Red Onion", "රතු ලූනු", "5", "pcs"),
                ing("Dried Chili", "වියළි මිරිස්", "3", "pcs"),
                ing("Mustard Seeds", "අබ", "0.5", "tsp"),
                ing("Grated Coconut", "ගාගත් පොල්", "0.5", "cup"),
                ing("Curry Leaves", "කරපිංචා", "1", "sprig")
            )),
            instructions = "1. Soak chickpeas overnight and boil with salt until tender.\n2. Temper mustard seeds, sliced onion, broken dried chili, and curry leaves in coconut oil.\n3. Toss in drained chickpeas and serve with grated coconut.",
            instructionsSi = "1. කඩල රැයක් පොඟවා ලුණු දමා තම්බා ගන්න.\n2. අබ, රතු ලූනු, වියළි මිරිස් සහ කරපිංචා පොල් තෙලෙන් තෙම්පරාදු කර කඩල එකතු කර ගාගත් පොල් සමඟ පිළිගන්වන්න.",
            prepTime = 20,
            category = "Breakfast",
            servings = 4
        ),
        RecipeEntity(
            name = "Soya Meat Devilled Curry",
            nameSi = "සෝයා මීට් තෙල් දැමීම",
            ingredientsJson = serializeIngredients(listOf(
                ing("Soya Meat", "සෝයා මීට්", "1", "pkt"),
                ing("Onion", "ලූනු", "1", "large"),
                ing("Tomato", "තක්කාලි", "1", "medium"),
                ing("Chili Flakes", "කෑලි මිරිස්", "1.5", "tbsp"),
                ing("Tomato Sauce", "තක්කාලි සෝස්", "2", "tbsp"),
                ing("Curry Leaves", "කරපිංචා", "1", "sprig")
            )),
            instructions = "1. Soak soya chunks in hot water 10 mins, squeeze dry.\n2. Pan-fry soya chunks with turmeric until lightly crisp.\n3. Stir-fry onion, curry leaves, tomato, chili flakes, and sauce, then toss in soya.",
            instructionsSi = "1. සෝයා මීට් උණු වතුරේ පොඟවා හොඳින් මිරිකා ගන්න.\n2. කහ අතගා තෙලේ බැදගන්න.\n3. ලූනු, කරපිංචා, තක්කාලි, කෑලි මිරිස් සහ සෝස් තෙම්පරාදු කර සෝයා එකතු කරන්න.",
            prepTime = 20,
            category = "Curry",
            servings = 4
        ),
        RecipeEntity(
            name = "Vegetable Fried Rice",
            nameSi = "එළවළු ෆ්‍රයිඩ් රයිස්",
            ingredientsJson = serializeIngredients(listOf(
                ing("Basmati Rice", "බාස්මතී සහල්", "2", "cups"),
                ing("Carrot", "කැරට්", "1", "medium"),
                ing("Leeks", "ලීක්ස්", "1", "stalk"),
                ing("Eggs", "බිත්තර", "2", "pcs"),
                ing("Garlic", "සුදු ලූනු", "3", "cloves"),
                ing("Soy Sauce", "සෝයා සෝස්", "1.5", "tbsp")
            )),
            instructions = "1. Cook and cool rice so grains stay separate.\n2. Stir-fry garlic, finely diced carrot, and leeks on high heat.\n3. Scramble eggs, add cooled rice, soy sauce, and white pepper, toss 4 mins.",
            instructionsSi = "1. බාස්මතී බත් පිස නිවාගන්න.\n2. සුදු ලූනු, කැරට් සහ ලීක්ස් අධික ගින්නේ තෙලේ බැදගන්න.\n3. බිත්තර, බත්, සෝයා සෝස් සහ ගම්මිරිස් දමා මිනිත්තු 4ක් කලවම් කරන්න.",
            prepTime = 30,
            category = "Dinner",
            servings = 4
        ),
        RecipeEntity(
            name = "Beetroot Curry",
            nameSi = "බීට්රූට් කරි",
            ingredientsJson = serializeIngredients(listOf(
                ing("Beetroot", "බීට්රූට්", "300", "g"),
                ing("Coconut Milk", "පොල් කිරි", "1", "cup"),
                ing("Onion", "ලූනු", "1", "small"),
                ing("Curry Powder", "තුනපහ", "1", "tsp"),
                ing("Curry Leaves", "කරපිංචා", "1", "sprig")
            )),
            instructions = "1. Peel and julienne beetroot.\n2. Simmer with onion, curry leaves, curry powder, and thin coconut milk for 15 mins.\n3. Stir in thick coconut milk and cook 4 mins.",
            instructionsSi = "1. බීට්රූට් සිහින්ව කපා ලූනු, කරපිංචා, තුනපහ සහ දිය කිරි දමා මිනිත්තු 15ක් තම්බා ගන්න.\n2. මිටි කිරි සහ ලුණු එකතු කර මිනිත්තු 4ක් පිසගන්න.",
            prepTime = 25,
            category = "Curry",
            servings = 4
        ),
        RecipeEntity(
            name = "Dhal & Spinach Curry",
            nameSi = "පරිප්පු සහ නිවිති",
            ingredientsJson = serializeIngredients(listOf(
                ing("Red Lentils (Dhal)", "පරිප්පු", "200", "g"),
                ing("Spinach (Nivithi)", "නිවිති", "1", "bunch"),
                ing("Coconut Milk", "පොල් කිරි", "1", "cup"),
                ing("Garlic", "සුදු ලූනු", "3", "cloves"),
                ing("Turmeric Powder", "කහ කුඩු", "0.5", "tsp")
            )),
            instructions = "1. Boil red lentils with garlic, onion, turmeric, and water until soft.\n2. Stir in chopped fresh spinach and coconut milk.\n3. Simmer 3 minutes until spinach just wilts.",
            instructionsSi = "1. පරිප්පු සුදු ලූනු, ලූනු සහ කහ සමඟ තම්බා ගන්න.\n2. කපාගත් නිවිති සහ පොල් කිරි එකතු කර මිනිත්තු 3ක් පිසගන්න.",
            prepTime = 20,
            category = "Greens",
            servings = 4
        ),
        RecipeEntity(
            name = "Potato White Curry (Ala Hodi)",
            nameSi = "අල කිරි හොදි",
            ingredientsJson = serializeIngredients(listOf(
                ing("Potato", "අල", "400", "g"),
                ing("Coconut Milk", "පොල් කිරි", "1.5", "cups"),
                ing("Onion", "ලූනු", "1", "small"),
                ing("Green Chili", "අමු මිරිස්", "2", "pcs"),
                ing("Turmeric Powder", "කහ කුඩු", "0.5", "tsp"),
                ing("Fenugreek Seeds", "උළුහාල්", "0.5", "tsp")
            )),
            instructions = "1. Peel and cube potatoes.\n2. Simmer in thin coconut milk with onion, green chili, fenugreek, and turmeric until tender.\n3. Pour thick coconut milk, gently stir, and finish with a squeeze of lime.",
            instructionsSi = "1. අල කෑලි කපා ලූනු, අමු මිරිස්, උළුහාල්, කහ සහ දිය කිරි දමා තම්බා ගන්න.\n2. මිටි කිරි එක් කර උතුරවා දෙහි බිංදු කිහිපයක් දමන්න.",
            prepTime = 25,
            category = "Curry",
            servings = 4
        ),
        RecipeEntity(
            name = "Polos Curry (Young Jackfruit)",
            nameSi = "පොළොස් ඇඹුල",
            ingredientsJson = serializeIngredients(listOf(
                ing("Young Jackfruit (Polos)", "පොළොස්", "500", "g"),
                ing("Roasted Curry Powder", "බැදපු තුනපහ", "2", "tbsp"),
                ing("Goraka (Garcinia)", "ගොරකා", "3", "pcs"),
                ing("Coconut Milk", "පොල් කිරි", "1.5", "cups"),
                ing("Garlic", "සුදු ලූනු", "5", "cloves"),
                ing("Curry Leaves", "කරපිංචා", "2", "sprigs")
            )),
            instructions = "1. Cut young jackfruit into chunks and rub with roasted curry powder, chili powder, garlic, and salt.\n2. Add goraka, curry leaves, and coconut milk in a clay pot.\n3. Slow-simmer 45–60 mins until deep brown and melt-in-the-mouth.",
            instructionsSi = "1. පොළොස් කැබලි වල බැදපු තුනපහ, මිරිස් කුඩු, සුදු ලූනු සහ ලුණු අතගාන්න.\n2. ගොරකා, කරපිංචා සහ පොල් කිරි දමා මැටි වළඳක මිනිත්තු 50ක් මද ගින්නේ හොඳින් හින්දා ගන්න.",
            prepTime = 60,
            category = "Curry",
            servings = 5
        ),
        RecipeEntity(
            name = "Sri Lankan Omelette & Toast",
            nameSi = "ඔම්ලට් සහ පාන් ටෝස්ට්",
            ingredientsJson = serializeIngredients(listOf(
                ing("Eggs", "බිත්තර", "3", "pcs"),
                ing("Red Onion", "රතු ලූනු", "3", "pcs"),
                ing("Green Chili", "අමු මිරිස්", "1", "pcs"),
                ing("Tomato", "තක්කාලි", "0.5", "pcs"),
                ing("Bread", "පාන්", "4", "slices")
            )),
            instructions = "1. Whisk eggs with finely diced onion, green chili, tomato, black pepper, and salt.\n2. Cook in a lightly oiled pan until fluffy and golden.\n3. Serve hot with toasted bread.",
            instructionsSi = "1. බිත්තර, සිහින්ව කපාගත් රතු ලූනු, අමු මිරිස්, තක්කාලි, ගම්මිරිස් සහ ලුණු හොඳින් ගසාගන්න.\n2. තෙල් ස්වල්පයක් දැමූ තැටියක බැද ටෝස්ට් කළ පාන් සමඟ පිළිගන්වන්න.",
            prepTime = 10,
            category = "Breakfast",
            servings = 2
        ),
        RecipeEntity(
            name = "Kola Kanda (Herbal Porridge)",
            nameSi = "කොළ කැඳ",
            ingredientsJson = serializeIngredients(listOf(
                ing("Gotukola Leaves", "ගොටුකොළ", "1", "bunch"),
                ing("Red Rice", "රතු කැකුළු සහල්", "0.5", "cup"),
                ing("Coconut Milk", "පොල් කිරි", "1.5", "cups"),
                ing("Garlic", "සුදු ලූනු", "2", "cloves"),
                ing("Jaggery", "හකුරු", "50", "g")
            )),
            instructions = "1. Boil red rice with garlic and water until very soft.\n2. Blend gotukola leaves with water and strain green juice.\n3. Stir coconut milk into rice, add green juice last, warm gently without boiling. Serve with jaggery.",
            instructionsSi = "1. රතු සහල් සුදු ලූනු සමඟ හොඳින් බෙරි වන තුරු පිසගන්න.\n2. ගොටුකොළ කොටා යුෂ මිරිකා ගන්න.\n3. බත් වලට පොල් කිරි සහ කොළ යුෂ එක් කර ලුණු දමා මද ගින්නේ රත් කර හකුරු සමඟ පානය කරන්න.",
            prepTime = 30,
            category = "Breakfast",
            servings = 4
        ),
        RecipeEntity(
            name = "Stir-Fried Noodles",
            nameSi = "නූඩ්ල්ස් තෙම්පරාදුව",
            ingredientsJson = serializeIngredients(listOf(
                ing("Noodles", "නූඩ්ල්ස්", "1", "pkt"),
                ing("Carrot", "කැරට්", "1", "medium"),
                ing("Leeks", "ලීක්ස්", "1", "stalk"),
                ing("Eggs", "බිත්තර", "2", "pcs"),
                ing("Onion", "ලූනු", "1", "small"),
                ing("Soy Sauce", "සෝයා සෝස්", "1", "tbsp")
            )),
            instructions = "1. Boil noodles per packet, drain and rinse cool.\n2. Stir-fry onion, carrot strips, leeks, and scrambled egg.\n3. Toss noodles with soy sauce and chili flakes for 3 mins.",
            instructionsSi = "1. නූඩ්ල්ස් තම්බා පෙරා ගන්න.\n2. ලූනු, කැරට්, ලීක්ස් සහ බිත්තර තෙලේ බැදගන්න.\n3. නූඩ්ල්ස්, සෝයා සෝස් සහ කෑලි මිරිස් දමා මිනිත්තු 3ක් කලවම් කරන්න.",
            prepTime = 20,
            category = "Dinner",
            servings = 3
        ),
        RecipeEntity(
            name = "Cabbage Mallum (Gova Mallum)",
            nameSi = "ගෝවා මැල්ලුම",
            ingredientsJson = serializeIngredients(listOf(
                ing("Cabbage", "ගෝවා", "300", "g"),
                ing("Grated Coconut", "ගාගත් පොල්", "0.75", "cup"),
                ing("Turmeric Powder", "කහ කුඩු", "0.5", "tsp"),
                ing("Mustard Seeds", "අබ", "0.5", "tsp"),
                ing("Green Chili", "අමු මිරිස්", "2", "pcs"),
                ing("Curry Leaves", "කරපිංචා", "1", "sprig")
            )),
            instructions = "1. Finely shred cabbage.\n2. Pop mustard seeds in 1 tsp oil with onion, chili, and curry leaves.\n3. Add cabbage, turmeric, salt, and grated coconut; stir-fry 5 mins until crisp-tender.",
            instructionsSi = "1. ගෝවා සිහින්ව ලියාගන්න.\n2. තෙල් ස්වල්පයක අබ, ලූනු, අමු මිරිස් සහ කරපිංචා බැදගන්න.\n3. ගෝවා, කහ, ලුණු සහ ගාගත් පොල් දමා මිනිත්තු 5ක් පිසගන්න.",
            prepTime = 15,
            category = "Greens",
            servings = 4
        ),
        RecipeEntity(
            name = "Oats & Banana Porridge",
            nameSi = "ඕට්ස් සහ කෙසෙල්",
            ingredientsJson = serializeIngredients(listOf(
                ing("Oats", "ඕට්ස්", "1", "cup"),
                ing("Milk", "කිරි", "1.5", "cups"),
                ing("Banana", "කෙසෙල්", "2", "pcs"),
                ing("Honey", "මී පැණි", "1", "tbsp")
            )),
            instructions = "1. Simmer oats with milk and a pinch of salt for 5 minutes.\n2. Top with sliced ripe banana and drizzle with honey or kithul treacle.",
            instructionsSi = "1. ඕට්ස් කිරි සමඟ මිනිත්තු 5ක් මද ගින්නේ පිසගන්න.\n2. කපාගත් කෙසෙල් සහ මී පැණි හෝ කිතුල් පැණි එකතු කර පිළිගන්වන්න.",
            prepTime = 10,
            category = "Breakfast",
            servings = 2
        ),
        RecipeEntity(
            name = "Mung Bean Milk Rice (Mung Kiribath)",
            nameSi = "මුං කිරිබත්",
            ingredientsJson = serializeIngredients(listOf(
                ing("Green Gram (Mung)", "මුං ඇට", "0.5", "cup"),
                ing("Red Rice", "රතු කැකුළු සහල්", "1.5", "cups"),
                ing("Coconut Milk", "පොල් කිරි", "2", "cups")
            )),
            instructions = "1. Dry-roast mung beans lightly, boil until half cooked.\n2. Add washed rice and water, cook until soft.\n3. Stir in thick coconut milk and salt, cook until rich, and cut into diamonds.",
            instructionsSi = "1. මුං ඇට මදක් බැද අඩක් තම්බා ගන්න.\n2. සෝදාගත් සහල් සහ වතුර දමා පිසගන්න.\n3. උකු පොල් කිරි සහ ලුණු දමා කිරි හිඳුණු පසු කැට කපාගන්න.",
            prepTime = 40,
            category = "Breakfast",
            servings = 5
        ),
        RecipeEntity(
            name = "Cashew Nut Curry (Kaju Maluwa)",
            nameSi = "කජු මාළුව",
            ingredientsJson = serializeIngredients(listOf(
                ing("Raw Cashew Nuts", "අමු කජු", "200", "g"),
                ing("Coconut Milk", "පොල් කිරි", "1.5", "cups"),
                ing("Onion", "ලූනු", "1", "small"),
                ing("Garlic", "සුදු ලූනු", "3", "cloves"),
                ing("Curry Powder", "තුනපහ", "1", "tsp"),
                ing("Turmeric Powder", "කහ කුඩු", "0.5", "tsp")
            )),
            instructions = "1. Soak cashews 2 hours and peel inner skins.\n2. Simmer with onion, garlic, curry leaves, curry powder, turmeric, and thin milk for 15 mins.\n3. Finish with thick coconut milk until creamy.",
            instructionsSi = "1. කජු පැය 2ක් පොඟවා කුරුට්ට ඉවත් කරන්න.\n2. ලූනු, සුදු ලූනු, කරපිංචා, තුනපහ, කහ සහ දිය කිරි දමා මිනිත්තු 15ක් තම්බා මිටි කිරි එක් කරන්න.",
            prepTime = 30,
            category = "Curry",
            servings = 4
        )
    )
}
