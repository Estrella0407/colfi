# Colfi

Colfi is an Android application for a coffee shop that allows users to browse the menu, place orders, and view their order history.

## Features

*   **User Authentication:** Users can sign up and log in to their accounts.
*   **Browse Menu:** View a list of available coffee and other items.
*   **Shopping Cart:** Add and remove items from the cart.
*   **Order Management:** Place orders and view past orders.
*   **User Profile:** View and manage user account information.

## Technologies Used

*   **UI:** Jetpack Compose
*   **Architecture:** MVVM (Model-View-ViewModel)
*   **Navigation:** Jetpack Navigation Compose
*   **Asynchronous Programming:** Kotlin Coroutines

## Libraries Used

*   **Firebase:** For analytics, authentication, and database services.
*   **Room:** For local data persistence.
*   **Coil:** For image loading.

## Project Structure

The project is organized into the following packages:

*   **data:** Contains data models, repositories, and data sources.
*   **navigation:** Handles navigation between screens using Jetpack Navigation Compose.
*   **ui:** Contains all the UI components, including screens, view models, and themes.
*   **di:** Handles dependency injection.

## How to Build

1.  Clone the repository:
    ```bash
    git clone https://github.com/your-username/colfi.git
    ```
2.  Open the project in Android Studio.
3.  Build and run the app on an Android emulator or a physical device.
