from create_sdk import create_icure_sdk
from icure import IcureSdk
from modules.codification import manage_codifications
from modules.examination import manage_examination
from modules.patient import manage_patient
from modules.search import search_patients_contacts_services
from modules.share_hcp import share_with_hcp
from modules.share_patient import share_with_patient


def login() -> IcureSdk:
    username = input("Username: ")
    password = input("Password: ")
    try:
        return create_icure_sdk(username, password)
    except Exception as e:
        print(f"Something went wrong: {e}")
        return login()


def choose_menu() -> int:
    print("0. Exit")
    print("1. Create a patient")
    print("2. Create medical data")
    print("3. Search")
    print("4. Share data with another hcp")
    print("5. Share data with a patient")
    print("6. Manage codifications")
    menu_choice = input("Enter your choice: ")
    try:
        return int(menu_choice)
    except ValueError:
        return choose_menu()


if __name__ == "__main__":
    sdk = login()
    choice = choose_menu()
    while choice != 0:
        if choice == 1:
            manage_patient(sdk)
        elif choice == 2:
            manage_examination(sdk)
        elif choice == 3:
            search_patients_contacts_services(sdk)
        elif choice == 4:
            share_with_hcp(sdk)
        elif choice == 5:
            share_with_patient(sdk)
        elif choice == 6:
            manage_codifications(sdk)
        choice = choose_menu()
