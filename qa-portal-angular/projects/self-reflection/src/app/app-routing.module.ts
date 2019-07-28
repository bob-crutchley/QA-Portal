import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import {AppComponent} from './app.component';
import { TrainerReflectionComponent } from './trainer-reflection/trainer-reflection.component';
import {TrainerCohortsComponent} from './trainer-cohorts/trainer-cohorts.component';

const routes: Routes = [
    {
        path: 'trainer',
        children: [
            {
                path: 'cohorts', component: TrainerCohortsComponent
            },
          {
            path: 'selfreflection/:id', component: TrainerReflectionComponent
          }
        ]
    }
];


@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
