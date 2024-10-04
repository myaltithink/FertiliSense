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

# Actions of handling logging of cycles
class ActionLogMenstrualCycle(Action):
    def name(self) -> str:
        return "action_log_menstrual_cycle"

    def run(self, dispatcher: CollectingDispatcher, tracker: Tracker, domain: dict):
        start_date = tracker.get_slot("start_date")
        end_date = tracker.get_slot("end_date")
        cycle_duration = tracker.get_slot("cycle_duration")
        period_duration = tracker.get_slot("period_duration")

        # Debug logs
        print(f"DEBUG: Start Date Slot Value: {start_date}")
        print(f"DEBUG: End Date Slot Value: {end_date}")
        print(f"DEBUG: Cycle Duration Slot Value: {cycle_duration}")
        print(f"DEBUG: Period Duration Slot Value: {period_duration}")

        if not start_date or not end_date or not cycle_duration or not period_duration:
            dispatcher.utter_message(text="Please provide all required information: start date, end date, cycle duration, and period duration.")
            print("DEBUG: Missing slot value(s).")
            return []

        user_id = tracker.sender_id

        try:
            # Calculate dates
            start_date_dt = datetime.strptime(start_date, "%d/%m/%Y")
            end_date_dt = datetime.strptime(end_date, "%d/%m/%Y")
            cycle_duration_days = int(cycle_duration)
            period_duration_days = int(period_duration)

            print(f"DEBUG: Start Date (datetime): {start_date_dt}")
            print(f"DEBUG: End Date (datetime): {end_date_dt}")

            # Fetch previous cycles from Firestore
            previous_cycles = db.collection('menstrual_cycles').document(user_id).get().to_dict()
            if previous_cycles:
                historical_cycles = previous_cycles.get('cycles', [])
            else:
                historical_cycles = []

            # Add the current cycle
            historical_cycles.append({
                'start_date': start_date,
                'end_date': end_date,
                'cycle_duration': cycle_duration,
                'period_duration': period_duration
            })

            # Calculate strong flow durations based on historical data
            strong_flow_durations = []
            for cycle in historical_cycles:
                cycle_start = datetime.strptime(cycle['start_date'], "%d/%m/%Y")
                cycle_end = datetime.strptime(cycle['end_date'], "%d/%m/%Y")
                period_duration = (cycle_end - cycle_start).days + 1
                strong_flow_start = cycle_start + timedelta(days=(period_duration // 3))
                # Assuming we need to use the user's data to determine the end of the strong flow
                strong_flow_end = strong_flow_start + timedelta(days=min(2, period_duration // 3))  # Ensure the end is within the period duration
                strong_flow_duration = (strong_flow_end - strong_flow_start).days + 1
                strong_flow_durations.append(strong_flow_duration)

            # Average strong flow duration
            if strong_flow_durations:
                average_strong_flow_duration = sum(strong_flow_durations) / len(strong_flow_durations)
            else:
                average_strong_flow_duration = 2  # Default if no historical data

            # Calculate predictions based on historical data
            predictions = []
            current_start_date = start_date_dt

            # Determine the start date for prediction based on historical data
            if historical_cycles:
                last_cycle = historical_cycles[-1]
                last_start_date = datetime.strptime(last_cycle['start_date'], "%d/%m/%Y")
                current_start_date = last_start_date + timedelta(days=int(last_cycle['cycle_duration']))

            for _ in range(4):  # Predict for the next 4 cycles
                cycle_end_date_dt = current_start_date + timedelta(days=period_duration_days)
                cycle_end_date = cycle_end_date_dt.strftime("%d/%m/%Y")

                next_cycle_start_date_dt = current_start_date + timedelta(days=cycle_duration_days)
                next_cycle_start_date = next_cycle_start_date_dt.strftime("%d/%m/%Y")

                ovulation_date_dt = next_cycle_start_date_dt - timedelta(days=14)
                fertile_window_start_dt = ovulation_date_dt - timedelta(days=2)
                fertile_window_end_dt = ovulation_date_dt + timedelta(days=2)

                fertile_window_start = fertile_window_start_dt.strftime("%d/%m/%Y")
                fertile_window_end = fertile_window_end_dt.strftime("%d/%m/%Y")

                # Calculate predicted ovulation date
                ovulation_date = ovulation_date_dt.strftime("%d/%m/%Y")

                # Calculate strong flow for the current cycle
                strong_flow_start_dt = current_start_date + timedelta(days=(period_duration_days // 3))
                strong_flow_end_dt = strong_flow_start_dt + timedelta(days=int(average_strong_flow_duration) - 1)
                strong_flow_start = strong_flow_start_dt.strftime("%d/%m/%Y")
                strong_flow_end = strong_flow_end_dt.strftime("%d/%m/%Y")

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

                # Prepare for next cycle prediction
                current_start_date = next_cycle_start_date_dt

            # Save to Firestore
            db.collection('menstrual_cycles').document(user_id).set({
                'cycles': historical_cycles,
                'predictions': predictions
            })
            dispatcher.utter_message(text="Your menstrual cycle information and predictions for the next four months have been recorded successfully.")
        except ValueError as e:
            dispatcher.utter_message(text="There was an error processing the dates. Please use the format dd/mm/yyyy.")
            print(f"ERROR: {e}")

        return []

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
class ActionItchyTesticles(Action):
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