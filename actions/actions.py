from datetime import datetime, timedelta
import firebase_admin
from firebase_admin import credentials, firestore
from rasa_sdk import Action, Tracker
from rasa_sdk.executor import CollectingDispatcher
from rasa_sdk.events import SlotSet

# Initialize Firebase
cred = credentials.Certificate("fertilisense-f1335-firebase-adminsdk-erc4z-f6c9372187.json")
firebase_admin.initialize_app(cred)
db = firestore.client()

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
                "startDate": start_date,
                "endDate": end_date,
                "cycleDuration": cycle_duration,
                "nextPredictedStartDate": next_start_date,
                "fertileWindowStart": fertile_window_start,
                "fertileWindowEnd": fertile_window_end,
                "timestamp": datetime.now().isoformat()
            }

            print(f"DEBUG: Data to be saved: {data}")

            db.collection("MenstrualCycles").document(user_id).collection("cycles").add(data)

            dispatcher.utter_message(text=f"Menstrual cycle logged. Your next menstruation is predicted to start on {next_start_date}. Your fertile window is from {fertile_window_start} to {fertile_window_end}.")
            print("DEBUG: Successfully saved data.")

        except Exception as e:
            print(f"DEBUG: Error: {e}")
            dispatcher.utter_message(text="An error occurred while logging your menstrual cycle.")

        return [SlotSet("start_date", None), SlotSet("end_date", None), SlotSet("cycle_duration", None)]
