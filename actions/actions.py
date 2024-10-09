from datetime import datetime, timedelta
from typing import Any, Text, Dict, List
import firebase_admin
from firebase_admin import credentials, firestore
from rasa_sdk import Action, Tracker
from rasa_sdk.executor import CollectingDispatcher
from rasa_sdk.events import SlotSet
import wikipediaapi
import openai
import requests
import json
from urllib.parse import quote

# Initialize Firebase
cred = credentials.Certificate("fertilisense-f1335-firebase-adminsdk-erc4z-f6c9372187.json")
firebase_admin.initialize_app(cred)
db = firestore.client()

# Initialize Wikipedia API with a custom user agent
wiki_wiki = wikipediaapi.Wikipedia(
    language='en',
    user_agent='FertiliSense/1.0 (Mobile App; Contact: ybiza2018@gmail.com)'
)

# Set up OpenAI API key
openai.api_key = ''

class ActionGreet(Action):
    def name(self) -> str:
        return "action_greet"

    def run(self, dispatcher: CollectingDispatcher, tracker: Tracker, domain: dict):
        dispatcher.utter_message(text="Hello! How can I help you today?")
        return []

class ActionGoodbye(Action):
    def name(self) -> str:
        return "action_goodbye"

    def run(self, dispatcher: CollectingDispatcher, tracker: Tracker, domain: dict):
        dispatcher.utter_message(text="Goodbye! Have a great day!")
        return []
    
class ActionThankYou(Action):
    def name(self) -> str:
        return "action_thank_you"

    def run(self, dispatcher: CollectingDispatcher, tracker: Tracker, domain: dict):
        dispatcher.utter_message(text="You're welcome mate!")
        return []

# Actions handle the mood
class ActionHandleMood(Action):    
    def name(self) -> Text:
        return "action_handle_mood"

    def run(self, dispatcher: CollectingDispatcher,
            tracker: Tracker,
            domain: Dict[Text, Any]) -> List[Dict[Text, Any]]:

        dispatcher.utter_message(response="utter_ask_mood")

        return []

# Action for getting info on Wikipedia
class ActionQueryWikipedia(Action):
    def name(self) -> str:
        return "action_query_wikipedia"

    def run(self, dispatcher: CollectingDispatcher, tracker: Tracker, domain: dict):
        topic = tracker.latest_message.get('text').lower()

        # List of topics related to the male reproductive system (anatomy, physiology, diseases)
        male_reproductive_topics = [
            'male reproductive system', 'testosterone', 'spermatogenesis', 'testes', 
            'epididymis', 'vas deferens', 'seminal vesicle', 'prostate', 'penis', 
            'scrotum', 'bulbourethral glands', 'semen', 'sperm', 'ejaculation',
            'erection', 'circumcision', 'foreskin', 'male infertility', 'erectile dysfunction', 
            'prostate cancer', 'benign prostatic hyperplasia', 'varicocele', 'hydrocele', 
            'testicular cancer', 'testicular torsion', 'hypogonadism', 'andropause',
            'oligospermia', 'azoospermia', 'cryptorchidism'
        ]

        # List of topics related to the female reproductive system (anatomy, physiology, diseases)
        female_reproductive_topics = [
            'female reproductive system', 'estrogen', 'progesterone', 'ovulation', 'ovaries', 
            'fallopian tubes', 'uterus', 'endometrium', 'myometrium', 'cervix', 'vagina', 
            'labia', 'clitoris', 'hymen', 'bartholin glands', 'skene glands', 'menstruation', 
            'menstrual cycle', 'follicular phase', 'luteal phase', 'menopause', 
            'perimenopause', 'female infertility', 'polycystic ovary syndrome', 
            'endometriosis', 'uterine fibroids', 'ovarian cysts', 'cervical cancer', 
            'ovarian cancer', 'uterine cancer', 'vaginitis', 'pelvic inflammatory disease', 
            'vulvodynia', 'dyspareunia', 'amenorrhea', 'dysmenorrhea', 'premenstrual syndrome (PMS)',
            'premenstrual dysphoric disorder (PMDD)', 'ectopic pregnancy', 'miscarriage', 
            'menorrhagia', 'polymenorrhea', 'oligomenorrhea'
        ]

        # Reproductive health and fertility-related topics
        fertility_and_health_topics = [
            'fertility', 'infertility', 'fertility treatments', 'IVF', 'artificial insemination',
            'surrogacy', 'fertility preservation', 'egg freezing', 'sperm donation', 
            'egg donation', 'ovarian reserve', 'fertility tests', 'hormonal tests', 
            'semen analysis', 'ultrasound for fertility', 'assisted reproductive technology',
            'contraception', 'birth control', 'condoms', 'oral contraceptives', 
            'intrauterine device (IUD)', 'hormonal implants', 'sterilization', 'vasectomy', 
            'tubal ligation', 'natural family planning', 'emergency contraception', 
            'morning-after pill', 'fertility awareness', 'reproductive rights', 
            'sexual health', 'sexually transmitted infections', 'HIV/AIDS', 'chlamydia', 
            'gonorrhea', 'syphilis', 'herpes', 'human papillomavirus (HPV)', 'hepatitis B', 
            'trichomoniasis', 'genital warts', 'infertility in men', 'infertility in women', 
            'sex education', 'safe sex', 'fertility clinics', 'male contraceptives', 
            'hormone replacement therapy', 'sexual dysfunction', 'libido', 'impotence', 
            'premature ejaculation', 'delayed ejaculation', 'vaginismus', 'reproductive endocrinology',
            'andrology', 'gynecology', 'urology', 'reproductive system'
        ]

        # Pregnancy and childbirth-related topics
        pregnancy_and_childbirth_topics = [
            'pregnancy', 'gestation', 'fertilization', 'zygote', 'embryo', 'fetus', 'prenatal care',
            'antenatal care', 'obstetrics', 'midwifery', 'natural birth', 'cesarean section', 
            'epidural', 'labor and delivery', 'childbirth', 'postpartum period', 'lactation', 
            'breastfeeding', 'colostrum', 'miscarriage', 'stillbirth', 'ectopic pregnancy', 
            'gestational diabetes', 'preeclampsia', 'placenta previa', 'placental abruption', 
            'preterm birth', 'post-term pregnancy', 'in vitro fertilization', 'assisted delivery',
            'home birth', 'water birth', 'doula', 'birth defects', 'genetic counseling', 
            'amniocentesis', 'chorionic villus sampling', 'ultrasound in pregnancy', 'fetal monitoring'
        ]

        # Combine all topics in one list
        reproductive_system_topics = (
            male_reproductive_topics + 
            female_reproductive_topics + 
            fertility_and_health_topics + 
            pregnancy_and_childbirth_topics
        )

        # Check if the topic is relevant to any of the reproductive system topics
        matched_topic = next((t for t in reproductive_system_topics if t in topic), None)

        if matched_topic:
            page = wiki_wiki.page(matched_topic)
            if page.exists():
                response = page.summary[:500]  # Return a short summary of the page
                dispatcher.utter_message(text=response)
            else:
                dispatcher.utter_message(text=f"Sorry, I couldn't find information on {matched_topic}.")
        else:
            dispatcher.utter_message(text="I'm sorry, I don't have information on that topic.")
        
        return []

# Action for getting info on GPT-4
class ActionQueryGPT4(Action):
    def name(self) -> str:
        return "action_query_gpt4"

    def run(self, dispatcher: CollectingDispatcher, tracker: Tracker, domain: dict):
        topic = tracker.latest_message.get('text')
        prompt = f"Provide detailed information on the following topic: {topic}"

        response = openai.ChatCompletion.create(
            model="gpt-3.5-turbo",
            messages=[
                {"role": "system", "content": "You are a helpful assistant."},
                {"role": "user", "content": prompt}
            ],
            max_tokens=150
        )

        result = response['choices'][0]['message']['content'].strip()
        dispatcher.utter_message(text=result)
        return []
    
# Actions for handling drug medicine using openFDA
class ActionFetchDrugInfo(Action):
    def name(self) -> Text:
        return "action_fetch_drug_info"

    def run(self, dispatcher: CollectingDispatcher,
            tracker: Tracker,
            domain: Dict[Text, Any]) -> List[Dict[Text, Any]]:

        query = tracker.get_slot("drug_query")
        
        if not query:
            dispatcher.utter_message(text="Please provide a drug name to get information.")
            return []

        api_key = "LrfhiuHJ6kLg03oGvZnwwY0j8yPhY2edeomjMgmo"
        # Encode the query to handle special characters
        encoded_query = quote(query)
        api_url = f"https://api.fda.gov/drug/event.json?api_key={api_key}&search={encoded_query}+AND+reproductive&limit=5"
        
        try:
            response = requests.get(api_url)
            response.raise_for_status()  # Raise HTTPError for bad responses
            data = response.json()

            if 'results' in data and len(data['results']) > 0:
                try:
                    drug_info = data['results'][0]['patient']['drug'][0].get('drugindication', 'No specific indication found.')
                    answer = f"The drug indication for {query} related to the reproductive system is: {drug_info}"
                except (KeyError, IndexError):
                    answer = "I couldn't find specific drug information due to a data error."
            else:
                answer = "I couldn't find information about that drug related to the reproductive system."

        except requests.RequestException as e:
            answer = "I'm sorry, I couldn't fetch the information right now."

        dispatcher.utter_message(text=answer)
        return []

# Action for asking user how is their day
class ActionAskDay(Action):
    def name(self) -> Text:
        return "action_ask_day"

    def run(self, dispatcher: CollectingDispatcher,
            tracker: Tracker,
            domain: Dict[Text, Any]) -> List[Dict[Text, Any]]:

        dispatcher.utter_message(response="utter_day")

        return []
                
# Action for Head Symptoms along with types
class ActionHeadSymptom(Action):
    def name(self) -> Text:
        return "action_head_symptom"

    def run(self, dispatcher: CollectingDispatcher,
            tracker: Tracker,
            domain: Dict[Text, Any]) -> List[Dict[Text, Any]]:

        dispatcher.utter_message(response="utter_head_symptom")

        return []

class ActionHeadWhole(Action):
    def name(self) -> Text:
        return "action_head_whole"

    def run(self, dispatcher: CollectingDispatcher,
            tracker: Tracker,
            domain: Dict[Text, Any]) -> List[Dict[Text, Any]]:

        dispatcher.utter_message(response="utter_head_whole_response")

        return []

class ActionHeadSides(Action):
    def name(self) -> Text:
        return "action_head_sides"

    def run(self, dispatcher: CollectingDispatcher,
            tracker: Tracker,
            domain: Dict[Text, Any]) -> List[Dict[Text, Any]]:

        dispatcher.utter_message(response="utter_head_sides_response")

        return []

class ActionHeadCrown(Action):
    def name(self) -> Text:
        return "action_head_crown"

    def run(self, dispatcher: CollectingDispatcher,
            tracker: Tracker,
            domain: Dict[Text, Any]) -> List[Dict[Text, Any]]:

        dispatcher.utter_message(response="utter_head_crown_response")

        return []

class ActionHeadEyes(Action):
    def name(self) -> Text:
        return "action_head_eyes"

    def run(self, dispatcher: CollectingDispatcher,
            tracker: Tracker,
            domain: Dict[Text, Any]) -> List[Dict[Text, Any]]:

        dispatcher.utter_message(response="utter_head_eyes_response")

        return []

# Action for Flu
class ActionFluSymptom(Action):
    def name(self) -> Text:
        return "action_flu_symptom"

    def run(self, dispatcher: CollectingDispatcher,
            tracker: Tracker,
            domain: Dict[Text, Any]) -> List[Dict[Text, Any]]:

        dispatcher.utter_message(response="utter_flu_response")

        return []

# Action for Vision
class ActionVisionSymptom(Action):
    def name(self) -> Text:
        return "action_vision_symptom"

    def run(self, dispatcher: CollectingDispatcher,
            tracker: Tracker,
            domain: Dict[Text, Any]) -> List[Dict[Text, Any]]:

        dispatcher.utter_message(response="utter_vision_types")

        return []

# Action for Stomach
class ActionStomachSymptom(Action):
    def name(self) -> Text:
        return "action_stomach_symptom"

    def run(self, dispatcher: CollectingDispatcher,
            tracker: Tracker,
            domain: Dict[Text, Any]) -> List[Dict[Text, Any]]:

        dispatcher.utter_message(response="utter_stomach_types")

        return []
    
# Actions for handling lifestyle of men
class ActionLifestyleMen(Action):
    def name(self) -> Text:
        return "action_lifestyle_men"

    def run(self, dispatcher: CollectingDispatcher,
            tracker: Tracker,
            domain: Dict[Text, Any]) -> List[Dict[Text, Any]]:

        dispatcher.utter_message(response="utter_nine_life_style_men")

        return []
    
# Actions for handling itchy testicles
class ActionItchyTesticles(Action):
    def name(self) -> Text:
        return "action_itchy_testicles"

    def run(self, dispatcher: CollectingDispatcher,
            tracker: Tracker,
            domain: Dict[Text, Any]) -> List[Dict[Text, Any]]:

        dispatcher.utter_message(response="utter_men_itchy_testicles")

        return []
    
# Actions for handling common causes of testicular pain
class ActionCommonCauses(Action):
    def name(self) -> Text:
        return "action_common_causes"

    def run(self, dispatcher: CollectingDispatcher,
            tracker: Tracker,
            domain: Dict[Text, Any]) -> List[Dict[Text, Any]]:

        dispatcher.utter_message(response="utter_men_common_causes")

        return []
    
# Actions for handling sperm health
class ActionSpermHealth(Action):
    def name(self) -> Text:
        return "action_sperm_health"

    def run(self, dispatcher: CollectingDispatcher,
            tracker: Tracker,
            domain: Dict[Text, Any]) -> List[Dict[Text, Any]]:

        dispatcher.utter_message(response="utter_men_sperm_health")

        return []
    
# Actions for handling good signs of healthy eggs
class ActionEggsHealthy(Action):
    def name(self) -> Text:
        return "action_eggs_healthy"

    def run(self, dispatcher: CollectingDispatcher,
            tracker: Tracker,
            domain: Dict[Text, Any]) -> List[Dict[Text, Any]]:

        dispatcher.utter_message(response="utter_good_quality_eggs")

        return []
    
# Actions for handling signs of a poor egg quality
class ActionEggsPoorQuality(Action):
    def name(self) -> Text:
        return "action_eggs_poor_quality"

    def run(self, dispatcher: CollectingDispatcher,
            tracker: Tracker,
            domain: Dict[Text, Any]) -> List[Dict[Text, Any]]:

        dispatcher.utter_message(response="utter_poor_quality_eggs")

        return []

# Actions for handling enhance quality of eggs
class ActionEggsEnhanceQuality(Action):
    def name(self) -> Text:
        return "action_eggs_enhance_quality"

    def run(self, dispatcher: CollectingDispatcher,
            tracker: Tracker,
            domain: Dict[Text, Any]) -> List[Dict[Text, Any]]:

        dispatcher.utter_message(response="utter_enhanced_quality_eggs")

        return []
    
# Actions for handling 10 foods for healthy eggs
class ActionEggsHealthyFoods(Action):
    def name(self) -> Text:
        return "action_eggs_healthy_foods"

    def run(self, dispatcher: CollectingDispatcher,
            tracker: Tracker,
            domain: Dict[Text, Any]) -> List[Dict[Text, Any]]:

        dispatcher.utter_message(response="utter_healthy_eggs_foods")

        return []
    
# Actions for handling why beans is healthy for eggs
class ActionEggsBeansHealthy(Action):
    def name(self) -> Text:
        return "action_eggs_beans_healthy"

    def run(self, dispatcher: CollectingDispatcher,
            tracker: Tracker,
            domain: Dict[Text, Any]) -> List[Dict[Text, Any]]:

        dispatcher.utter_message(response="utter_beans_healthy_eggs")

        return []
    
# Actions for handling why seeds is healthy for eggs
class ActionEggsSeedsHealthy(Action):
    def name(self) -> Text:
        return "action_eggs_seeds_healthy"

    def run(self, dispatcher: CollectingDispatcher,
            tracker: Tracker,
            domain: Dict[Text, Any]) -> List[Dict[Text, Any]]:

        dispatcher.utter_message(response="utter_seeds_healthy_eggs")

        return []
    
# Actions for handling why nuts and dry fruits is healthy for eggs
class ActionEggsNutsHealthy(Action):
    def name(self) -> Text:
        return "action_eggs_nuts_healthy"

    def run(self, dispatcher: CollectingDispatcher,
            tracker: Tracker,
            domain: Dict[Text, Any]) -> List[Dict[Text, Any]]:

        dispatcher.utter_message(response="utter_nuts_healthy_eggs")

        return []
    
# Actions for handling why avocados is healthy for eggs
class ActionEggsAvocadosHealthy(Action):
    def name(self) -> Text:
        return "action_eggs_avocados_healthy"

    def run(self, dispatcher: CollectingDispatcher,
            tracker: Tracker,
            domain: Dict[Text, Any]) -> List[Dict[Text, Any]]:

        dispatcher.utter_message(response="utter_avocados_healthy_eggs")

        return []

# Actions for handling why berries is healthy for eggs
class ActionEggsBerriesHealthy(Action):
    def name(self) -> Text:
        return "action_eggs_berries_healthy"

    def run(self, dispatcher: CollectingDispatcher,
            tracker: Tracker,
            domain: Dict[Text, Any]) -> List[Dict[Text, Any]]:

        dispatcher.utter_message(response="utter_berries_healthy_eggs")

        return []

# Actions for handling why cinnamon is healthy for eggs
class ActionEggsCinnamonHealthy(Action):
    def name(self) -> Text:
        return "action_eggs_cinnamon_healthy"

    def run(self, dispatcher: CollectingDispatcher,
            tracker: Tracker,
            domain: Dict[Text, Any]) -> List[Dict[Text, Any]]:

        dispatcher.utter_message(response="utter_cinnamon_healthy_eggs")

        return []

# Actions for handling why ginger is healthy for eggs
class ActionEggsGingerHealthy(Action):
    def name(self) -> Text:
        return "action_eggs_ginger_healthy"

    def run(self, dispatcher: CollectingDispatcher,
            tracker: Tracker,
            domain: Dict[Text, Any]) -> List[Dict[Text, Any]]:

        dispatcher.utter_message(response="utter_ginger_healthy_eggs")

        return []

# Actions for handling why green leafy vegetables is healthy for eggs
class ActionEggsGreenLeafyHealthy(Action):
    def name(self) -> Text:
        return "action_eggs_green_leafy_healthy"

    def run(self, dispatcher: CollectingDispatcher,
            tracker: Tracker,
            domain: Dict[Text, Any]) -> List[Dict[Text, Any]]:

        dispatcher.utter_message(response="utter_green_leafy_healthy_eggs")

        return []

# Actions for handling why whole grains is healthy for eggs
class ActionEggsWholeGrainsHealthy(Action):
    def name(self) -> Text:
        return "action_eggs_whole_grains_healthy"

    def run(self, dispatcher: CollectingDispatcher,
            tracker: Tracker,
            domain: Dict[Text, Any]) -> List[Dict[Text, Any]]:

        dispatcher.utter_message(response="utter_whole_grains_healthy_eggs")

        return []

# Actions for handling why dairy is healthy for eggs
class ActionEggsDairyHealthy(Action):
    def name(self) -> Text:
        return "action_eggs_dairy_healthy"

    def run(self, dispatcher: CollectingDispatcher,
            tracker: Tracker,
            domain: Dict[Text, Any]) -> List[Dict[Text, Any]]:

        dispatcher.utter_message(response="utter_dairy_healthy_eggs")

        return []
    
# Actions for handling dietary changes to affect egg quality
class ActionDietaryChangesAffectHealthyEggs(Action):
    def name(self) -> Text:
        return "action_dietary_changes_affect_healthy_egg"

    def run(self, dispatcher: CollectingDispatcher,
            tracker: Tracker,
            domain: Dict[Text, Any]) -> List[Dict[Text, Any]]:

        dispatcher.utter_message(response="utter_dietary_changes_affect_healthy_eggs")

        return []

# Actions for handling foods to avoid improve egg quality
class ActionFoodAvoidImproveEggsQuality(Action):
    def name(self) -> Text:
        return "action_food_avoid_improve_eggs_quality"

    def run(self, dispatcher: CollectingDispatcher,
            tracker: Tracker,
            domain: Dict[Text, Any]) -> List[Dict[Text, Any]]:

        dispatcher.utter_message(response="utter_foods_to_avoid_improve_healthy_eggs")

        return []

# Action for handling organic and non-organic food
class ActionOrganicNonOrganicFoods(Action):
    def name(self) -> Text:
        return "action_organic_non_organic_foods_eggs"

    def run(self, dispatcher: CollectingDispatcher,
            tracker: Tracker,
            domain: Dict[Text, Any]) -> List[Dict[Text, Any]]:

        dispatcher.utter_message(response="utter_organic_and_non_organic_foods_eggs")

        return []

# Action for handling why egg quality is important
class ActionEggQualityImportant(Action):
    def name(self) -> Text:
        return "action_egg_quality_important"

    def run(self, dispatcher: CollectingDispatcher,
            tracker: Tracker,
            domain: Dict[Text, Any]) -> List[Dict[Text, Any]]:

        dispatcher.utter_message(response="utter_egg_quality_important")

        return []

# Action for handling top ten diet rules
class ActionTopTenDietRules(Action):
    def name(self) -> Text:
        return "action_top_ten_diet_rules"

    def run(self, dispatcher: CollectingDispatcher,
            tracker: Tracker,
            domain: Dict[Text, Any]) -> List[Dict[Text, Any]]:

        dispatcher.utter_message(response="utter_top_ten_diet_rules")

        return []

# Action for handling food increase sperm count
class ActionFoodIncreaseSpermCount(Action):
    def name(self) -> Text:
        return "action_food_increase_sperm_count"

    def run(self, dispatcher: CollectingDispatcher,
            tracker: Tracker,
            domain: Dict[Text, Any]) -> List[Dict[Text, Any]]:

        dispatcher.utter_message(response="utter_food_increased_sperm_count")

        return []

# Action for handling ways to increase sperm count
class ActionIncreaseSpermCountWays(Action):
    def name(self) -> Text:
        return "action_increase_sperm_count_ways"

    def run(self, dispatcher: CollectingDispatcher,
            tracker: Tracker,
            domain: Dict[Text, Any]) -> List[Dict[Text, Any]]:

        dispatcher.utter_message(response="utter_ways_to_increase_sperm_count")

        return []

# Action for handling what is d-aspartic acid supplements
class ActionDAsparticAcidSupplements(Action):
    def name(self) -> Text:
        return "action_d_aspartic_acid_supplements"

    def run(self, dispatcher: CollectingDispatcher,
            tracker: Tracker,
            domain: Dict[Text, Any]) -> List[Dict[Text, Any]]:

        dispatcher.utter_message(response="utter_d_aspartic_acid_supplements")

        return []

# Action for handling what is tribulus terrestris
class ActionTribulusTerrestris(Action):
    def name(self) -> Text:
        return "action_tribulus_terrestris"

    def run(self, dispatcher: CollectingDispatcher,
            tracker: Tracker,
            domain: Dict[Text, Any]) -> List[Dict[Text, Any]]:

        dispatcher.utter_message(response="utter_tribulus_terrestris")

        return []
    
# Action for handling what is fenugreek supplements
class ActionFenugreekSupplements(Action):
    def name(self) -> Text:
        return "action_fenugreek_supplements"

    def run(self, dispatcher: CollectingDispatcher,
            tracker: Tracker,
            domain: Dict[Text, Any]) -> List[Dict[Text, Any]]:

        dispatcher.utter_message(response="utter_fenugreek_supplements")

        return []
    
# Action for handling what is ashwagandha
class ActionAshwagandha(Action):
    def name(self) -> Text:
        return "action_ashwagandha"

    def run(self, dispatcher: CollectingDispatcher,
            tracker: Tracker,
            domain: Dict[Text, Any]) -> List[Dict[Text, Any]]:

        dispatcher.utter_message(response="utter_ashwagandha")

        return []

# Action for handling what is maca root
class ActionMacaRoot(Action):
    def name(self) -> Text:
        return "action_maca_root"

    def run(self, dispatcher: CollectingDispatcher,
            tracker: Tracker,
            domain: Dict[Text, Any]) -> List[Dict[Text, Any]]:

        dispatcher.utter_message(response="utter_maca_root")

        return []

# Action for handling how can you find out your baby's sex
class ActionFindBabySex(Action):
    def name(self) -> Text:
        return "action_find_baby_sex"

    def run(self, dispatcher: CollectingDispatcher,
            tracker: Tracker,
            domain: Dict[Text, Any]) -> List[Dict[Text, Any]]:

        dispatcher.utter_message(response="utter_how_to_find_baby_sex")

        return []

# Action for handling how accurate is IVF with sex selection
class ActionIVFSexSelectionAccuracy(Action):
    def name(self) -> Text:
        return "action_ivf_sex_selection_accuracy"

    def run(self, dispatcher: CollectingDispatcher,
            tracker: Tracker,
            domain: Dict[Text, Any]) -> List[Dict[Text, Any]]:

        dispatcher.utter_message(response="utter_accuracy_of_ivf_with_sex_selection")

        return []

# Action for handling what is NIPT and how does it work
class ActionNIPTAndWork(Action):
    def name(self) -> Text:
        return "action_nipt_and_work"

    def run(self, dispatcher: CollectingDispatcher,
            tracker: Tracker,
            domain: Dict[Text, Any]) -> List[Dict[Text, Any]]:

        dispatcher.utter_message(response="utter_nipt_and_how_it_works")

        return []

# Action for handling what are the benefits of nipt
class ActionBenefitsOfNIPT(Action):
    def name(self) -> Text:
        return "action_benefits_of_nipt"

    def run(self, dispatcher: CollectingDispatcher,
            tracker: Tracker,
            domain: Dict[Text, Any]) -> List[Dict[Text, Any]]:

        dispatcher.utter_message(response="utter_benefits_of_nipt")

        return []

# Action for handling what is cvs and when is it performed
class ActionCVSAndWhen(Action):
    def name(self) -> Text:
        return "action_cvs_and_when"

    def run(self, dispatcher: CollectingDispatcher,
            tracker: Tracker,
            domain: Dict[Text, Any]) -> List[Dict[Text, Any]]:

        dispatcher.utter_message(response="utter_cvs_and_how_it_works")

        return []

# Action for handling what is amniocentesis and what does it reveal?
class ActionAmniocentesisAndReveal(Action):
    def name(self) -> Text:
        return "action_amniocentesis_and_reveal"

    def run(self, dispatcher: CollectingDispatcher,
            tracker: Tracker,
            domain: Dict[Text, Any]) -> List[Dict[Text, Any]]:

        dispatcher.utter_message(response="utter_amniocentesis_and_how_it_works")

        return []

# Action for handling when can you find out the baby's sex with an ultrasound
class ActionUltrasoundSexFind(Action):
    def name(self) -> Text:
        return "action_ultrasound_sex_find"

    def run(self, dispatcher: CollectingDispatcher,
            tracker: Tracker,
            domain: Dict[Text, Any]) -> List[Dict[Text, Any]]:

        dispatcher.utter_message(response="utter_when_to_find_baby_sex_with_ultrasound")

        return []

# Action for handling do gender predictor tests work?
class ActionGenderPredictorTestsWork(Action):
    def name(self) -> Text:
        return "action_gender_predictor_tests_work"

    def run(self, dispatcher: CollectingDispatcher,
            tracker: Tracker,
            domain: Dict[Text, Any]) -> List[Dict[Text, Any]]:

        dispatcher.utter_message(response="utter_gender_predictor_tests_work")

        return []

# Action for handling what is the gender identity pregnancy test
class ActionGenderIdentityPregnancyTest(Action):
    def name(self) -> Text:
        return "action_gender_identity_pregnancy_test"

    def run(self, dispatcher: CollectingDispatcher,
            tracker: Tracker,
            domain: Dict[Text, Any]) -> List[Dict[Text, Any]]:

        dispatcher.utter_message(response="utter_gender_identity_pregnancy_test")

        return []

# Action for handling can antibiotics cause your period to be late?
class ActionAntibioticsPeriodLate(Action):
    def name(self) -> Text:
        return "action_antibiotics_period_late"

    def run(self, dispatcher: CollectingDispatcher,
            tracker: Tracker,
            domain: Dict[Text, Any]) -> List[Dict[Text, Any]]:

        dispatcher.utter_message(response="utter_antibiotics_cause_period_to_be_late")

        return []

# Action for handling does taking antibiotics affect your period at all
class ActionAntibioticsPeriodImpact(Action):
    def name(self) -> Text:
        return "action_antibiotics_period_impact"

    def run(self, dispatcher: CollectingDispatcher,
            tracker: Tracker,
            domain: Dict[Text, Any]) -> List[Dict[Text, Any]]:

        dispatcher.utter_message(response="utter_taking_antibiotics_affects_period")

        return []

# Action for handling can stress from illness affect your period
class ActionStressIllnessPeriod(Action):
    def name(self) -> Text:
        return "action_stress_illness_period"

    def run(self, dispatcher: CollectingDispatcher,
            tracker: Tracker,
            domain: Dict[Text, Any]) -> List[Dict[Text, Any]]:

        dispatcher.utter_message(response="utter_stress_from_illness_affects_period")

        return []

# Action for handling what are the most common reasons your period may be late
class ActionCommonPeriodLateReasons(Action):
    def name(self) -> Text:
        return "action_common_period_late_reasons"

    def run(self, dispatcher: CollectingDispatcher,
            tracker: Tracker,
            domain: Dict[Text, Any]) -> List[Dict[Text, Any]]:

        dispatcher.utter_message(response="utter_most_common_reasons_for_period_late")

        return []

# Action for handling is there anything i can do about late periods
class ActionLatePeriodHelp(Action):
    def name(self) -> Text:
        return "action_late_period_help"

    def run(self, dispatcher: CollectingDispatcher,
            tracker: Tracker,
            domain: Dict[Text, Any]) -> List[Dict[Text, Any]]:

        dispatcher.utter_message(response="utter_anything_i_can_do_about_late_periods")

        return []

# Action for handling when should you consult a doctor about missed periods
class ActionConsultDoctorPeriodMissed(Action):
    def name(self) -> Text:
        return "action_consult_doctor_period_missed"

    def run(self, dispatcher: CollectingDispatcher,
            tracker: Tracker,
            domain: Dict[Text, Any]) -> List[Dict[Text, Any]]:

        dispatcher.utter_message(response="utter_when_to_consult_doctor_about_missed_periods")

        return []

# Action for handling what is ivf
class ActionIVF(Action):
    def name(self) -> Text:
        return "action_ivf"

    def run(self, dispatcher: CollectingDispatcher,
            tracker: Tracker,
            domain: Dict[Text, Any]) -> List[Dict[Text, Any]]:

        dispatcher.utter_message(response="utter_ivf")

        return []

# Action for handling what is icsi
class ActionICSI(Action):
    def name(self) -> Text:
        return "action_icsi"

    def run(self, dispatcher: CollectingDispatcher,
            tracker: Tracker,
            domain: Dict[Text, Any]) -> List[Dict[Text, Any]]:

        dispatcher.utter_message(response="utter_icsi")

        return []

# Action for handling what is the procedure of IVF
class ActionIVFProcedure(Action):
    def name(self) -> Text:
        return "action_ivf_procedure"

    def run(self, dispatcher: CollectingDispatcher,
            tracker: Tracker,
            domain: Dict[Text, Any]) -> List[Dict[Text, Any]]:

        dispatcher.utter_message(response="utter_procedure_of_ivf")

        return []

# Action for handling drinks and foods that harm male fertility
class ActionDrinksAndFoods(Action):
    def name(self) -> Text:
        return "action_drinks_and_foods_that_harm_male_fertility"

    def run(self, dispatcher: CollectingDispatcher,
            tracker: Tracker,
            domain: Dict[Text, Any]) -> List[Dict[Text, Any]]:

        dispatcher.utter_message(response="utter_drinks_and_foods_that_harm_male_fertility")

        return []

# Action for handling things that could be messing with your guys sperm
class ActionThingsMessingWithSperm(Action):
    def name(self) -> Text:
        return "action_things_messing_with_sperm"

    def run(self, dispatcher: CollectingDispatcher,
            tracker: Tracker,
            domain: Dict[Text, Any]) -> List[Dict[Text, Any]]:

        dispatcher.utter_message(response="utter_things_that_could_be_messing_with_guys_sperm")

        return []

# Action for handling what physical changes happen after childbirth
class ActionPhysicalChangesAfterBirth(Action):
    def name(self) -> Text:
        return "action_physical_changes_after_birth"

    def run(self, dispatcher: CollectingDispatcher,
            tracker: Tracker,
            domain: Dict[Text, Any]) -> List[Dict[Text, Any]]:

        dispatcher.utter_message(response="utter_physical_changes_after_childbirth")

        return []

# Action for handling what is a c-section
class ActionCSection(Action):
    def name(self) -> Text:
        return "action_c_section"

    def run(self, dispatcher: CollectingDispatcher,
            tracker: Tracker,
            domain: Dict[Text, Any]) -> List[Dict[Text, Any]]:

        dispatcher.utter_message(response="utter_c_section")

        return []

# Action for handling why c sections is done
class ActionWhyCSection(Action):
    def name(self) -> Text:
        return "action_why_c_section"

    def run(self, dispatcher: CollectingDispatcher,
            tracker: Tracker,
            domain: Dict[Text, Any]) -> List[Dict[Text, Any]]:

        dispatcher.utter_message(response="utter_why_c_section")

        return []

# Action for handling what happens after c section
class ActionAfterCSection(Action):
    def name(self) -> Text:
        return "action_after_c_section"

    def run(self, dispatcher: CollectingDispatcher,
            tracker: Tracker,
            domain: Dict[Text, Any]) -> List[Dict[Text, Any]]:

        dispatcher.utter_message(response="utter_after_c_section")

        return []

# Action for handling how long to wait before cleaning up after sex
class ActionCleaningUpAfterSex(Action):
    def name(self) -> Text:
        return "action_cleaning_up_after_sex"

    def run(self, dispatcher: CollectingDispatcher,
            tracker: Tracker,
            domain: Dict[Text, Any]) -> List[Dict[Text, Any]]:

        dispatcher.utter_message(response="utter_how_long_to_wait_before_cleaning_up_after_sex")

        return []

# Action for handling vagina care
class ActionVaginaCare(Action):
    def name(self) -> Text:
        return "action_vagina_care"

    def run(self, dispatcher: CollectingDispatcher,
            tracker: Tracker,
            domain: Dict[Text, Any]) -> List[Dict[Text, Any]]:

        dispatcher.utter_message(response="utter_vagina_care")

        return []

# Action for handling penis care
class ActionPenisCare(Action):
    def name(self) -> Text:
        return "action_penis_care"

    def run(self, dispatcher: CollectingDispatcher,
            tracker: Tracker,
            domain: Dict[Text, Any]) -> List[Dict[Text, Any]]:

        dispatcher.utter_message(response="utter_penis_care")

        return []

# Action for handling anal sex hygiene
class ActionAnalSexHygiene(Action):
    def name(self) -> Text:
        return "action_anal_sex_hygiene"

    def run(self, dispatcher: CollectingDispatcher,
            tracker: Tracker,
            domain: Dict[Text, Any]) -> List[Dict[Text, Any]]:

        dispatcher.utter_message(response="utter_anal_sex_hygiene")

        return []

# Action for handling oral sex hygiene 
class ActionOralSexHygiene(Action):
    def name(self) -> Text:
        return "action_oral_sex_hygiene"

    def run(self, dispatcher: CollectingDispatcher,
            tracker: Tracker,
            domain: Dict[Text, Any]) -> List[Dict[Text, Any]]:

        dispatcher.utter_message(response="utter_oral_sex_hygiene")

        return []
    
# Actions for handling cleaning sex toys
class ActionCleaningSexToys(Action):
    def name(self) -> Text:
        return "action_cleaning_sex_toys"

    def run(self, dispatcher: CollectingDispatcher,
            tracker: Tracker,
            domain: Dict[Text, Any]) -> List[Dict[Text, Any]]:

        dispatcher.utter_message(response="utter_cleaning_sex_toys")

        return []

# Actions for handling cleaning bed
class ActionCleaningBed(Action):
    def name(self) -> Text:
        return "action_cleaning_bed"

    def run(self, dispatcher: CollectingDispatcher,
            tracker: Tracker,
            domain: Dict[Text, Any]) -> List[Dict[Text, Any]]:

        dispatcher.utter_message(response="utter_cleaning_bed")

        return []

# Actions for handling trying to conceived
class ActionTryingToConceive(Action):
    def name(self) -> Text:
        return "action_trying_to_conceive"

    def run(self, dispatcher: CollectingDispatcher,
            tracker: Tracker,
            domain: Dict[Text, Any]) -> List[Dict[Text, Any]]:

        dispatcher.utter_message(response="utter_trying_to_conceive")

        return []

# Actions for handling cleaning vagina prevent sex
class ActionCleaningVaginaPreventSex(Action):
    def name(self) -> Text:
        return "action_cleaning_vagina_prevent_sex"

    def run(self, dispatcher: CollectingDispatcher,
            tracker: Tracker,
            domain: Dict[Text, Any]) -> List[Dict[Text, Any]]:

        dispatcher.utter_message(response="utter_cleaning_vagina_prevent_sex")

        return []

# Actions for handling prevent infection after sex
class ActionPreventInfectionAfterSex(Action):
    def name(self) -> Text:
        return "action_prevent_infection_after_sex"

    def run(self, dispatcher: CollectingDispatcher,
            tracker: Tracker,
            domain: Dict[Text, Any]) -> List[Dict[Text, Any]]:

        dispatcher.utter_message(response="utter_prevent_infection_after_sex")

        return []

# Actions for handling best time to get pregnant
class ActionBestTimeToGetPregnant(Action):
    def name(self) -> Text:
        return "action_best_time_to_get_pregnant"

    def run(self, dispatcher: CollectingDispatcher,
            tracker: Tracker,
            domain: Dict[Text, Any]) -> List[Dict[Text, Any]]:

        dispatcher.utter_message(response="utter_best_time_to_get_pregnant")

        return []

# Actions for handling miscarriage
class ActionMiscarriage(Action):
    def name(self) -> Text:
        return "action_miscarriage"

    def run(self, dispatcher: CollectingDispatcher,
            tracker: Tracker,
            domain: Dict[Text, Any]) -> List[Dict[Text, Any]]:

        dispatcher.utter_message(response="utter_miscarriage")

        return []

# Actions for handling types of miscarriage
class ActionTypesOfMiscarriage(Action):
    def name(self) -> Text:
        return "action_types_of_miscarriage"

    def run(self, dispatcher: CollectingDispatcher,
            tracker: Tracker,
            domain: Dict[Text, Any]) -> List[Dict[Text, Any]]:

        dispatcher.utter_message(response="utter_types_of_miscarriage")

        return []

# Actions for handling missed miscarriage
class ActionMissedMiscarriage(Action):
    def name(self) -> Text:
        return "action_missed_miscarriage"

    def run(self, dispatcher: CollectingDispatcher,
            tracker: Tracker,
            domain: Dict[Text, Any]) -> List[Dict[Text, Any]]:

        dispatcher.utter_message(response="utter_missed_miscarriage")

        return []

# Actions for handling complete miscarriage
class ActionCompleteMiscarriage(Action):
    def name(self) -> Text:
        return "action_complete_miscarriage"

    def run(self, dispatcher: CollectingDispatcher,
            tracker: Tracker,
            domain: Dict[Text, Any]) -> List[Dict[Text, Any]]:

        dispatcher.utter_message(response="utter_complete_miscarriage")

        return []

# Actions for handling recurrent miscarriage
class ActionRecurrentMiscarriage(Action):
    def name(self) -> Text:
        return "action_recurrent_miscarriage"

    def run(self, dispatcher: CollectingDispatcher,
            tracker: Tracker,
            domain: Dict[Text, Any]) -> List[Dict[Text, Any]]:

        dispatcher.utter_message(response="utter_recurrent_miscarriage")

        return []

# Actions for handling threatened miscarriage
class ActionThreatenedMiscarriage(Action):
    def name(self) -> Text:
        return "action_threatened_miscarriage"

    def run(self, dispatcher: CollectingDispatcher,
            tracker: Tracker,
            domain: Dict[Text, Any]) -> List[Dict[Text, Any]]:

        dispatcher.utter_message(response="utter_threatened_miscarriage")

        return []

# Actions for handling inevitable miscarriage
class ActionInevitableMiscarriage(Action):
    def name(self) -> Text:
        return "action_inevitable_miscarriage"

    def run(self, dispatcher: CollectingDispatcher,
            tracker: Tracker,
            domain: Dict[Text, Any]) -> List[Dict[Text, Any]]:

        dispatcher.utter_message(response="utter_inevitable_miscarriage")

        return []

# Actions for handling having miscarriage
class ActionHavingMiscarriage(Action):
    def name(self) -> Text:
        return "action_having_miscarriage"

    def run(self, dispatcher: CollectingDispatcher,
            tracker: Tracker,
            domain: Dict[Text, Any]) -> List[Dict[Text, Any]]:

        dispatcher.utter_message(response="utter_having_miscarriage")

        return []

# Actions for handling cause miscarriage
class ActionCauseMiscarriage(Action):
    def name(self) -> Text:
        return "action_cause_miscarriage"

    def run(self, dispatcher: CollectingDispatcher,
            tracker: Tracker,
            domain: Dict[Text, Any]) -> List[Dict[Text, Any]]:

        dispatcher.utter_message(response="utter_cause_miscarriage")

        return []

# Actions for handling painful miscarriage
class ActionPainfulMiscarriage(Action):
    def name(self) -> Text:
        return "action_painful_miscarriage"

    def run(self, dispatcher: CollectingDispatcher,
            tracker: Tracker,
            domain: Dict[Text, Any]) -> List[Dict[Text, Any]]:

        dispatcher.utter_message(response="utter_painful_miscarriage")

        return []

# Actions for handling safe sex
class ActionSafeSex(Action):
    def name(self) -> Text:
        return "action_safe_sex"

    def run(self, dispatcher: CollectingDispatcher,
            tracker: Tracker,
            domain: Dict[Text, Any]) -> List[Dict[Text, Any]]:

        dispatcher.utter_message(response="utter_safe_sex")

        return []

# Actions for handling importance safe sex
class ActionImportanceSafeSex(Action):
    def name(self) -> Text:
        return "action_importance_safe_sex"

    def run(self, dispatcher: CollectingDispatcher,
            tracker: Tracker,
            domain: Dict[Text, Any]) -> List[Dict[Text, Any]]:

        dispatcher.utter_message(response="utter_importance_safe_sex")

        return []

# Actions for handling sti
class ActionSti(Action):
    def name(self) -> Text:
        return "action_sti"
    
    def run(self, dispatcher: CollectingDispatcher,
            tracker: Tracker,
            domain: Dict[Text, Any]) -> List[Dict[Text, Any]]:
        
        dispatcher.utter_message(response="utter_sti")

        return []

# Actions for handling std
class ActionStd(Action):
    def name(self) -> Text:
        return "action_std"
    
    def run(self, dispatcher: CollectingDispatcher,
            tracker: Tracker,
            domain: Dict[Text, Any]) -> List[Dict[Text, Any]]:
        
        dispatcher.utter_message(response="utter_std")

        return []

# Actions for handling difference std sti
class ActionDifferenceStdSti(Action):
    def name(self) -> Text:
        return "action_difference_std_sti"
    
    def run(self, dispatcher: CollectingDispatcher,
            tracker: Tracker,
            domain: Dict[Text, Any]) -> List[Dict[Text, Any]]:
        
        dispatcher.utter_message(response="utter_difference_std_sti")

        return []

# Actions for handling type sti
class ActionTypeSti(Action):
    def name(self) -> Text:
        return "action_types_sti"
    
    def run(self, dispatcher: CollectingDispatcher,
            tracker: Tracker,
            domain: Dict[Text, Any]) -> List[Dict[Text, Any]]:
        
        dispatcher.utter_message(response="utter_types_sti")

        return []

# Actions for handling how common sti
class ActionHowCommonSti(Action):
    def name(self) -> Text:
        return "action_how_common_sti"
    
    def run(self, dispatcher: CollectingDispatcher,
            tracker: Tracker,
            domain: Dict[Text, Any]) -> List[Dict[Text, Any]]:
        
        dispatcher.utter_message(response="utter_how_common_sti")

        return []

# Actions for handling symptoms sti
class ActionSymptomsSti(Action):
    def name(self) -> Text:
        return "action_symptoms_sti"
    
    def run(self, dispatcher: CollectingDispatcher,
            tracker: Tracker,
            domain: Dict[Text, Any]) -> List[Dict[Text, Any]]:
        
        dispatcher.utter_message(response="utter_symptoms_sti")

        return []

# Actions for handling causes sti
class ActionCausesSti(Action):
    def name(self) -> Text:
        return "action_causes_sti"
    
    def run(self, dispatcher: CollectingDispatcher,
            tracker: Tracker,
            domain: Dict[Text, Any]) -> List[Dict[Text, Any]]:
        
        dispatcher.utter_message(response="utter_causes_sti")

        return []

# Actions for handling sti contagious
class ActionStiContagious(Action):
    def name(self) -> Text:
        return "action_sti_contagious"
    
    def run(self, dispatcher: CollectingDispatcher,
            tracker: Tracker,
            domain: Dict[Text, Any]) -> List[Dict[Text, Any]]:
        
        dispatcher.utter_message(response="utter_sti_contagious")

        return []

# Actions for handling sti diagnosed
class ActionStiDiagnosed(Action):
    def name(self) -> Text:
        return "action_sti_diagnosed"
    
    def run(self, dispatcher: CollectingDispatcher,
            tracker: Tracker,
            domain: Dict[Text, Any]) -> List[Dict[Text, Any]]:
        
        dispatcher.utter_message(response="utter_sti_diagnosed")

        return []

# Actions for handling sti testing
class ActionStiTesting(Action):
    def name(self) -> Text:
        return "action_sti_testing"
    
    def run(self, dispatcher: CollectingDispatcher,
            tracker: Tracker,
            domain: Dict[Text, Any]) -> List[Dict[Text, Any]]:
        
        dispatcher.utter_message(response="utter_sti_testing")

        return []

# Actions for handling sexual health
class ActionSexualHealth(Action):
    def name(self) -> Text:
        return "action_sexual_health"
    
    def run(self, dispatcher: CollectingDispatcher,
            tracker: Tracker,
            domain: Dict[Text, Any]) -> List[Dict[Text, Any]]:
        
        dispatcher.utter_message(response="utter_sexual_health")

        return []

# Actions for handling importance sexual health
class ActionImportanceSexualHealth(Action):
    def name(self) -> Text:
        return "action_importance_sexual_health"
    
    def run(self, dispatcher: CollectingDispatcher,
            tracker: Tracker,
            domain: Dict[Text, Any]) -> List[Dict[Text, Any]]:
        
        dispatcher.utter_message(response="utter_importance_sexual_health")

        return []

# Actions for handling hiv
class ActionHiv(Action):
    def name(self) -> Text:
        return "action_hiv"
    
    def run(self, dispatcher: CollectingDispatcher,
            tracker: Tracker,
            domain: Dict[Text, Any]) -> List[Dict[Text, Any]]:
        
        dispatcher.utter_message(response="utter_hiv")

        return []

# Actions for handling hiv treatment
class ActionHivTreatment(Action):
    def name(self) -> Text:
        return "action_hiv_treatment"
    
    def run(self, dispatcher: CollectingDispatcher,
            tracker: Tracker,
            domain: Dict[Text, Any]) -> List[Dict[Text, Any]]:
        
        dispatcher.utter_message(response="utter_hiv_treatment")

        return []

# Actions for handling medication during pregnancy
class ActionMedicationPregnancy(Action):
    def name(self) -> Text:
        return "action_medication_during_pregnancy"
    
    def run(self, dispatcher: CollectingDispatcher,
            tracker: Tracker,
            domain: Dict[Text, Any]) -> List[Dict[Text, Any]]:
        
        dispatcher.utter_message(response="utter_medication_during_pregnancy")

        return []
   
class ActionDeleteMenstrualCycle(Action):

    def name(self) -> str:
        return "action_delete_menstrual_cycle"

    def run(self, dispatcher: CollectingDispatcher, tracker: Tracker, domain: dict):
        # Extract the old start and end dates from slots
        old_start_date = tracker.get_slot("old_start_date")
        old_end_date = tracker.get_slot("old_end_date")
        
        user_id = tracker.sender_id

        try:
            # Fetch previous cycles from Firestore
            previous_cycles = db.collection('menstrual_cycles').document(user_id).get().to_dict()
            historical_cycles = previous_cycles.get('cycles', []) if previous_cycles else []

            # Find cycles to delete using old_start_date or old_end_date
            cycles_to_delete = []
            for cycle in historical_cycles:
                # Check if cycle matches based on either old start or old end date
                if (old_start_date and cycle.get('start_date') == old_start_date) or \
                   (old_end_date and cycle.get('end_date') == old_end_date):
                    cycles_to_delete.append(cycle)

            if not cycles_to_delete:
                dispatcher.utter_message(text="No cycles found with the specified old start or end date.")
                return []

            # Remove the found cycles from historical_cycles
            for cycle in cycles_to_delete:
                historical_cycles.remove(cycle)

            # Save updated cycles back to Firestore
            db.collection('menstrual_cycles').document(user_id).set({
                'cycles': historical_cycles,
                'predictions': previous_cycles.get('predictions', [])
            })

            # If there are still cycles left, recalculate predictions
            if historical_cycles:
                new_cycle_duration = historical_cycles[-1].get('cycle_duration')
                new_period_duration = historical_cycles[-1].get('period_duration')
                predictions = self.predict_next_cycles(historical_cycles, new_cycle_duration, new_period_duration)

                # Save the new predictions to Firestore
                db.collection('menstrual_cycles').document(user_id).set({
                    'predictions': predictions
                }, merge=True)

            dispatcher.utter_message(text="Your menstrual cycle has been deleted successfully, and predictions have been updated.")
        except Exception as e:
            dispatcher.utter_message(text="There was an error deleting the cycle.")
            print(f"ERROR: {e}")

        return []

    def predict_next_cycles(self, historical_cycles, new_cycle_duration, new_period_duration):
        # This method contains the logic for predicting next cycles based on the provided durations
        predictions = []
        current_start_date = datetime.strptime(historical_cycles[-1]['start_date'], "%d/%m/%Y") + timedelta(days=int(new_cycle_duration))

        for _ in range(4):  # Predict for the next 4 cycles
            cycle_end_date_dt = current_start_date + timedelta(days=int(new_period_duration) - 1)
            cycle_end_date = cycle_end_date_dt.strftime("%d/%m/%Y")

            next_cycle_start_date_dt = current_start_date + timedelta(days=int(new_cycle_duration))
            next_cycle_start_date = next_cycle_start_date_dt.strftime("%d/%m/%Y")

            ovulation_date_dt = next_cycle_start_date_dt - timedelta(days=14)
            ovulation_date = ovulation_date_dt.strftime("%d/%m/%Y")

            fertile_window_start_dt = ovulation_date_dt - timedelta(days=2)
            fertile_window_end_dt = ovulation_date_dt + timedelta(days=2)
            fertile_window_start = fertile_window_start_dt.strftime("%d/%m/%Y")
            fertile_window_end = fertile_window_end_dt.strftime("%d/%m/%Y")

            # Corrected strong flow calculation: starts on the 2nd day of the period and lasts 2 days
            strong_flow_start_dt = current_start_date + timedelta(days=1)  # Start strong flow on the 2nd day of the period
            strong_flow_end_dt = strong_flow_start_dt + timedelta(days=1)  # End on the 3rd day of the period
            strong_flow_start = strong_flow_start_dt.strftime("%d/%m/%Y")
            strong_flow_end = strong_flow_end_dt.strftime("%d/%m/%Y")

            # Add this cycle's prediction
            predictions.append({
                'cycle_start_date': current_start_date.strftime("%d/%m/%Y"),
                'cycle_end_date': cycle_end_date,
                'fertile_window_start': fertile_window_start,
                'fertile_window_end': fertile_window_end,
                'next_cycle_start_date': next_cycle_start_date,
                'ovulation_date': ovulation_date,
                'strong_flow_start': strong_flow_start,
                'strong_flow_end': strong_flow_end
            })

            # Update current_start_date to the next cycle's start date for the next prediction
            current_start_date = next_cycle_start_date_dt

        return predictions

class ActionUpdateMenstrualCycle(Action):
    def name(self) -> str:
        return "action_update_menstrual_cycle"

    def run(self, dispatcher: CollectingDispatcher, tracker: Tracker, domain: dict):
        # Extract slot values for updating the cycle
        old_start_date = tracker.get_slot("old_start_date")
        old_end_date = tracker.get_slot("old_end_date")
        new_start_date = tracker.get_slot("new_start_date")
        new_end_date = tracker.get_slot("new_end_date")
        new_cycle_duration = tracker.get_slot("new_cycle_duration")
        new_period_duration = tracker.get_slot("new_period_duration")

        user_id = tracker.sender_id

        try:
            # Fetch previous cycles from Firestore
            previous_cycles = db.collection('menstrual_cycles').document(user_id).get().to_dict()
            historical_cycles = previous_cycles.get('cycles', []) if previous_cycles else []

            # Find cycle to update using old_start_date or old_end_date
            cycle_to_update = None
            for cycle in historical_cycles:
                # Check if cycle matches based on either old start or end date
                if (old_start_date and cycle.get('start_date') == old_start_date) or \
                   (old_end_date and cycle.get('end_date') == old_end_date):
                    cycle_to_update = cycle
                    break

            if cycle_to_update is None:
                dispatcher.utter_message(text="No cycle found with the specified old start or end date.")
                return []

            # Update the cycle found based on provided new dates
            if new_start_date:
                cycle_to_update['start_date'] = new_start_date
            if new_end_date:
                cycle_to_update['end_date'] = new_end_date
            if new_cycle_duration:
                cycle_to_update['cycle_duration'] = new_cycle_duration
            if new_period_duration:
                cycle_to_update['period_duration'] = new_period_duration

            # Save updated cycles back to Firestore
            db.collection('menstrual_cycles').document(user_id).set({
                'cycles': historical_cycles,
                'predictions': previous_cycles.get('predictions', [])
            })

            # Trigger predictions after updating the cycle
            predictions = self.predict_next_cycles(historical_cycles, new_cycle_duration, new_period_duration)

            # Save the new predictions
            db.collection('menstrual_cycles').document(user_id).set({
                'predictions': predictions
            }, merge=True)

            dispatcher.utter_message(text="Your menstrual cycle has been updated successfully and predictions recalculated.")
        except Exception as e:
            dispatcher.utter_message(text="There was an error updating the cycle.")
            print(f"ERROR: {e}")

        return []

    def predict_next_cycles(self, historical_cycles, new_cycle_duration, new_period_duration):
        # This method contains the logic for predicting next cycles based on the provided durations
        predictions = []
        current_start_date = datetime.strptime(historical_cycles[-1]['start_date'], "%d/%m/%Y") + timedelta(days=int(new_cycle_duration))

        for _ in range(4):  # Predict for the next 4 cycles
            cycle_end_date_dt = current_start_date + timedelta(days=int(new_period_duration) - 1)
            cycle_end_date = cycle_end_date_dt.strftime("%d/%m/%Y")

            next_cycle_start_date_dt = current_start_date + timedelta(days=int(new_cycle_duration))
            next_cycle_start_date = next_cycle_start_date_dt.strftime("%d/%m/%Y")

            ovulation_date_dt = next_cycle_start_date_dt - timedelta(days=14)
            ovulation_date = ovulation_date_dt.strftime("%d/%m/%Y")

            fertile_window_start_dt = ovulation_date_dt - timedelta(days=2)
            fertile_window_end_dt = ovulation_date_dt + timedelta(days=2)
            fertile_window_start = fertile_window_start_dt.strftime("%d/%m/%Y")
            fertile_window_end = fertile_window_end_dt.strftime("%d/%m/%Y")

            # Corrected strong flow calculation: starts on the 2nd day of the period and lasts 2 days
            strong_flow_start_dt = current_start_date + timedelta(days=1)  # Start strong flow on the 2nd day of the period
            strong_flow_end_dt = strong_flow_start_dt + timedelta(days=1)  # End on the 3rd day of the period
            strong_flow_start = strong_flow_start_dt.strftime("%d/%m/%Y")
            strong_flow_end = strong_flow_end_dt.strftime("%d/%m/%Y")

            # Add this cycle's prediction
            predictions.append({
                'cycle_start_date': current_start_date.strftime("%d/%m/%Y"),
                'cycle_end_date': cycle_end_date,
                'fertile_window_start': fertile_window_start,
                'fertile_window_end': fertile_window_end,
                'next_cycle_start_date': next_cycle_start_date,
                'ovulation_date': ovulation_date,
                'strong_flow_start': strong_flow_start,
                'strong_flow_end': strong_flow_end
            })

            # Update current_start_date to the next cycle's start date for the next prediction
            current_start_date = next_cycle_start_date_dt

        return predictions
    
class ActionLogMenstrualCycle(Action):
    def name(self) -> str:
        return "action_log_menstrual_cycle"

    def run(self, dispatcher: CollectingDispatcher, tracker: Tracker, domain: dict):
        # Extracting slot values
        start_dates = tracker.get_slot("start_dates")
        end_dates = tracker.get_slot("end_dates")
        cycle_durations = tracker.get_slot("cycle_durations")
        period_durations = tracker.get_slot("period_durations")

        if not start_dates or not end_dates or not cycle_durations or not period_durations:
            dispatcher.utter_message(text="Please provide all required cycle information: start dates, end dates, cycle durations, and period durations.")
            return []

        user_id = tracker.sender_id

        try:
            # Initialize historical cycles list
            historical_cycles = []

            # Fetch previous cycles from Firestore
            previous_cycles = db.collection('menstrual_cycles').document(user_id).get().to_dict()
            if previous_cycles:
                historical_cycles = previous_cycles.get('cycles', [])

            # Add the current cycles
            for i in range(len(start_dates)):
                historical_cycles.append({
                    'start_date': start_dates[i],
                    'end_date': end_dates[i],
                    'cycle_duration': cycle_durations[i],
                    'period_duration': period_durations[i]
                })

            # Calculate strong flow durations based on historical data
            strong_flow_durations = []
            for cycle in historical_cycles:
                cycle_start = datetime.strptime(cycle['start_date'], "%d/%m/%Y")
                cycle_end = datetime.strptime(cycle['end_date'], "%d/%m/%Y")
                period_duration = (cycle_end - cycle_start).days + 1
                strong_flow_start = cycle_start + timedelta(days=(period_duration // 3))
                strong_flow_end = strong_flow_start + timedelta(days=min(2, period_duration // 3))  # Ensure the end is within the period duration
                strong_flow_duration = (strong_flow_end - strong_flow_start).days + 1
                strong_flow_durations.append(strong_flow_duration)

            # Average strong flow duration
            average_strong_flow_duration = sum(strong_flow_durations) / len(strong_flow_durations) if strong_flow_durations else 2  # Default if no historical data

            # Calculate predictions based on historical data
            predictions = []
            current_start_date = datetime.strptime(start_dates[-1], "%d/%m/%Y")  # Start with the latest logged cycle

            # Determine the next cycle's start date based on the last logged cycle
            if historical_cycles:
                last_cycle = historical_cycles[-1]
                cycle_duration_days = int(cycle_durations[-1])  # Use the last cycle duration
                
                # Calculate the start date for the first prediction based on the last logged cycle
                current_start_date = datetime.strptime(last_cycle['start_date'], "%d/%m/%Y") + timedelta(days=cycle_duration_days)  # Start of the next cycle
            else:
                # If no historical data, default to the most recent start date
                current_start_date = datetime.strptime(start_dates[-1], "%d/%m/%Y")

            for _ in range(4):  # Predict for the next 4 cycles
                period_duration_days = int(period_durations[-1])

                # Calculate cycle end date for the current cycle
                cycle_end_date_dt = current_start_date + timedelta(days=period_duration_days - 1)  # End date for the current cycle
                cycle_end_date = cycle_end_date_dt.strftime("%d/%m/%Y")

                # Predict the next cycle's start date based on the current cycle's start date and duration
                cycle_duration_days = int(cycle_durations[-1])  # Use the cycle duration of the last logged cycle
                next_cycle_start_date_dt = current_start_date + timedelta(days=cycle_duration_days)  # Start of the next cycle
                next_cycle_start_date = next_cycle_start_date_dt.strftime("%d/%m/%Y")

                # Ovulation is typically 14 days before the next cycle start date
                ovulation_date_dt = next_cycle_start_date_dt - timedelta(days=14)
                ovulation_date = ovulation_date_dt.strftime("%d/%m/%Y")

                # Fertile window is typically 2 days before and 2 days after ovulation
                fertile_window_start_dt = ovulation_date_dt - timedelta(days=2)
                fertile_window_end_dt = ovulation_date_dt + timedelta(days=2)
                fertile_window_start = fertile_window_start_dt.strftime("%d/%m/%Y")
                fertile_window_end = fertile_window_end_dt.strftime("%d/%m/%Y")

                # Calculate strong flow start and end dates
                strong_flow_start_dt = current_start_date + timedelta(days=(period_duration_days // 3))
                strong_flow_end_dt = strong_flow_start_dt + timedelta(days=int(average_strong_flow_duration) - 1)
                strong_flow_start = strong_flow_start_dt.strftime("%d/%m/%Y")
                strong_flow_end = strong_flow_end_dt.strftime("%d/%m/%Y")

                # Add this cycle's prediction
                predictions.append({
                    'cycle_start_date': current_start_date.strftime("%d/%m/%Y"),
                    'cycle_end_date': cycle_end_date,
                    'fertile_window_start': fertile_window_start,
                    'fertile_window_end': fertile_window_end,
                    'next_cycle_start_date': next_cycle_start_date,
                    'ovulation_date': ovulation_date,
                    'strong_flow_start': strong_flow_start,
                    'strong_flow_end': strong_flow_end
                })

                # Update current_start_date to the next cycle's start date for the next prediction
                current_start_date = next_cycle_start_date_dt

            # Save both historical cycles and predictions to Firestore
            db.collection('menstrual_cycles').document(user_id).set({
                'cycles': historical_cycles,
                'predictions': predictions
            })

            dispatcher.utter_message(text="Your menstrual cycle information and predictions for the next four cycles have been recorded successfully.")
        except ValueError as e:
            dispatcher.utter_message(text="There was an error processing the dates. Please use the format dd/mm/yyyy.")
            print(f"ERROR: {e}")

        return []