import { Component, EventEmitter, Output } from '@angular/core';

@Component({
  selector: 'app-add-patient-modal',
  templateUrl: './add-patient-modal.component.html',
  styleUrls: ['./add-patient-modal.component.css']
})
export class AddPatientModalComponent {

  @Output() close = new EventEmitter<void>();

  closeModal(): void {
    this.close.emit();
  }

  savePatient(): void {

    // Backend will be integrated later.
    // For now, we only close the modal.

    this.closeModal();
  }
}