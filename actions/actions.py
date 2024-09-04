from datetime import datetime, timedelta
from typing import Any, Text, Dict, List
import firebase_admin
from firebase_admin import credentials, firestore
from rasa_sdk import Action, Tracker
from rasa_sdk.executor import CollectingDispatcher
from rasa_sdk.events import SlotSet
import wikipediaapi
import openai

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
openai.api_key = 'your_api_key'

class ActionLogMenstrualCycle(Action):
    def name(self) -> str:
        return "action_log_menstrual_cycle"

    def run(self, dispatcher: CollectingDispatcher, tracker: Tracker, domain: dict):
        start_date = tracker.get_slot("start_date")
        end_date = tracker.get_slot("end_date")
        cycle_duration = tracker.get_slot("cycle_duration")

        # Enhanced debug logs
        print(f"DEBUG: Start Date Slot Value: {start_date}")
        print(f"DEBUG: End Date Slot Value: {end_date}")
        print(f"DEBUG: Cycle Duration Slot Value: {cycle_duration}")

        if not start_date or not end_date or not cycle_duration:
            dispatcher.utter_message(text="Please provide all required information: start date, end date, and cycle duration.")
            print("DEBUG: Missing slot value(s).")
            return []

        user_id = tracker.sender_id

        try:
            # Calculate dates and save to Firebase
            start_date_dt = datetime.strptime(start_date, "%d/%m/%Y")
            end_date_dt = datetime.strptime(end_date, "%d/%m/%Y")
            cycle_duration_days = int(cycle_duration)

            next_start_date_dt = start_date_dt + timedelta(days=cycle_duration_days)
            next_start_date = next_start_date_dt.strftime("%d/%m/%Y")

            ovulation_date_dt = next_start_date_dt - timedelta(days=14)
            fertile_window_start_dt = ovulation_date_dt - timedelta(days=2)
            fertile_window_end_dt = ovulation_date_dt + timedelta(days=2)

            fertile_window_start = fertile_window_start_dt.strftime("%d/%m/%Y")
            fertile_window_end = fertile_window_end_dt.strftime("%d/%m/%Y")

            data = {
                'start_date': start_date,
                'end_date': end_date,
                'cycle_duration': cycle_duration,
                'next_start_date': next_start_date,
                'fertile_window_start': fertile_window_start,
                'fertile_window_end': fertile_window_end
            }

            # Save to Firestore
            db.collection('menstrual_cycles').document(user_id).set(data)
            dispatcher.utter_message(text="Your menstrual cycle information has been recorded successfully.")
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

class ActionHandleMood(Action):
    def name(self) -> str:
        return "action_handle_mood"

    def run(self, dispatcher: CollectingDispatcher, tracker: Tracker, domain: dict):
        dispatcher.utter_message(text="It sounds like you're not feeling great. Do you want to talk about it?")
        return []

class ActionQueryWikipedia(Action):
    def name(self) -> str:
        return "action_query_wikipedia"

    def run(self, dispatcher: CollectingDispatcher, tracker: Tracker, domain: dict):
        topic = tracker.latest_message.get('text')
        if 'male reproductive system' in topic.lower():
            page = wiki_wiki.page('Male_reproductive_system')
        elif 'female reproductive system' in topic.lower():
            page = wiki_wiki.page('Female_reproductive_system')
        else:
            dispatcher.utter_message(text="I'm sorry, I don't have information on that topic.")
            return []

        response = page.summary[:500]  # Return a short summary
        dispatcher.utter_message(text=response)
        return []

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

class ActionAskDay(Action):
    def name(self) -> Text:
        return "action_ask_day"

    def run(self, dispatcher: CollectingDispatcher,
            tracker: Tracker,
            domain: Dict[Text, Any]) -> List[Dict[Text, Any]]:

        dispatcher.utter_message(response="utter_day")

        return []

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

class ActionFluSymptom(Action):
    def name(self) -> Text:
        return "action_flu_symptom"

    def run(self, dispatcher: CollectingDispatcher,
            tracker: Tracker,
            domain: Dict[Text, Any]) -> List[Dict[Text, Any]]:

        dispatcher.utter_message(response="utter_flu_response")

        return []

class ActionVisionSymptom(Action):
    def name(self) -> Text:
        return "action_vision_symptom"

    def run(self, dispatcher: CollectingDispatcher,
            tracker: Tracker,
            domain: Dict[Text, Any]) -> List[Dict[Text, Any]]:

        dispatcher.utter_message(response="utter_vision_types")

        return []

class ActionStomachSymptom(Action):
    def name(self) -> Text:
        return "action_stomach_symptom"

    def run(self, dispatcher: CollectingDispatcher,
            tracker: Tracker,
            domain: Dict[Text, Any]) -> List[Dict[Text, Any]]:

        dispatcher.utter_message(response="utter_stomach_types")

        return []