import { Component, OnInit } from '@angular/core';
import { GenericControlComponent } from '../generic-control/generic-control.component';

@Component({
  selector: 'app-radio-button',
  templateUrl: './radio-button.component.html',
  styleUrls: ['./radio-button.component.css']
})
export class RadioButtonComponent extends GenericControlComponent<string> implements OnInit  {
  
  ngOnInit(): void {
    this.question = {
      comment: '',
      ...this.question
    }
  }

  setRadioResponse(response: string): void {
    this.question.response = response;
    this.announceChange();
  }

}