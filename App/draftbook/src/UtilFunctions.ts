import {toastController} from "@ionic/vue";

/**
 * Present a toast notification to the user
 * @param message The message to display to the user.
 */
export const presentToast = async (message:string) => {
    const toast = await toastController.create({
        message: message,
        duration: 4000,
        position: 'bottom',
    });

    await toast.present();
};