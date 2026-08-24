import {AfterViewInit, Component, ElementRef, ViewChild} from '@angular/core';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [RouterLink],
  templateUrl: './home.component.html',
  styleUrl: './home.component.scss'
})
export class HomeComponent implements AfterViewInit {

  constructor() {}


  @ViewChild('heroCanvas')
  heroCanvas!: ElementRef<HTMLCanvasElement>;

  @ViewChild('showcaseCanvas')
  showcaseCanvas!: ElementRef<HTMLCanvasElement>;

  slice = 24;

  readonly totalSlices = 48;
  readonly lesionCenter = 24;
  readonly lesionSpan = 12;

  menuOpen = false;

  ngAfterViewInit(): void {
    this.drawDWISlice(
      this.heroCanvas.nativeElement,
      30,
      60,
      30,
      16,
      0.55
    );

    this.renderShowcase(this.slice);
  }

  toggleMenu(): void {
    this.menuOpen = !this.menuOpen;
  }

  closeMenu(): void {
    this.menuOpen = false;
  }

  onSliceChange(event: Event): void {
    const input = event.target as HTMLInputElement;

    this.slice = Number(input.value);

    this.renderShowcase(this.slice);
  }

  private renderShowcase(slice: number): void {
    this.drawDWISlice(
      this.showcaseCanvas.nativeElement,
      slice,
      this.totalSlices,
      this.lesionCenter,
      this.lesionSpan,
      0.55
    );
  }

  private drawDWISlice(
    canvas: HTMLCanvasElement,
    sliceIdx: number,
    totalSlices: number,
    lesionCenter: number,
    lesionSpan: number,
    opacity: number
  ): void {

    const ctx = canvas.getContext('2d');

    if (!ctx) {
      return;
    }

    const w = canvas.width;
    const h = canvas.height;

    const cx = w / 2;
    const cy = h / 2;

    ctx.clearRect(0, 0, w, h);

    const rand = (seed: number): number => {
      const x = Math.sin(seed) * 10000;
      return x - Math.floor(x);
    };

    const t = sliceIdx / totalSlices;
    const taper = Math.sin(Math.PI * t);

    const rx = w * 0.32 * (0.55 + 0.45 * taper);
    const ry = h * 0.38 * (0.55 + 0.45 * taper);

    /*
     * Background
     */
    ctx.fillStyle = '#05070c';
    ctx.fillRect(0, 0, w, h);

    /*
     * MRI radial gradient
     */
    const gradient = ctx.createRadialGradient(
      cx,
      cy,
      4,
      cx,
      cy,
      Math.max(rx, ry)
    );

    gradient.addColorStop(0, '#d7dbe2');
    gradient.addColorStop(0.55, '#9aa1ac');
    gradient.addColorStop(0.85, '#575c66');
    gradient.addColorStop(1, '#1b1d22');

    ctx.save();

    ctx.beginPath();

    ctx.ellipse(
      cx,
      cy,
      rx,
      ry,
      0,
      0,
      Math.PI * 2
    );

    ctx.closePath();

    ctx.fillStyle = gradient;
    ctx.fill();

    ctx.clip();

    /*
     * Brain structures
     */
    ctx.fillStyle = 'rgba(15,17,20,0.55)';

    ctx.beginPath();
    ctx.ellipse(
      cx - rx * 0.16,
      cy,
      rx * 0.11,
      ry * 0.34,
      0.15,
      0,
      Math.PI * 2
    );
    ctx.fill();

    ctx.beginPath();
    ctx.ellipse(
      cx + rx * 0.16,
      cy,
      rx * 0.11,
      ry * 0.34,
      -0.15,
      0,
      Math.PI * 2
    );
    ctx.fill();

    /*
     * MRI texture
     */
    for (let i = 0; i < 110; i++) {

      const rr = rand(i * 13.1 + sliceIdx * 0.7);
      const rr2 = rand(i * 7.7 + sliceIdx * 0.3 + 5);

      const angle = rr * Math.PI * 2;

      const distance =
        rr2 * Math.min(rx, ry) * 0.92;

      const px =
        cx +
        Math.cos(angle) *
        distance *
        (rx / Math.max(rx, ry));

      const py =
        cy +
        Math.sin(angle) *
        distance *
        (ry / Math.max(rx, ry));

      const shade =
        40 + rand(i * 3.3) * 70;

      ctx.fillStyle =
        `rgba(${shade + 90},${shade + 95},${shade + 100},0.25)`;

      ctx.beginPath();

      ctx.ellipse(
        px,
        py,
        6 + rand(i) * 10,
        2 + rand(i + 1) * 3,
        angle,
        0,
        Math.PI * 2
      );

      ctx.fill();
    }

    /*
     * Center line
     */
    ctx.strokeStyle = 'rgba(10,10,12,0.35)';
    ctx.lineWidth = 1.4;

    ctx.beginPath();
    ctx.moveTo(cx, cy - ry * 0.9);
    ctx.lineTo(cx, cy + ry * 0.9);
    ctx.stroke();

    ctx.restore();

    /*
     * Brain border
     */
    ctx.strokeStyle = 'rgba(230,233,238,0.35)';
    ctx.lineWidth = 2;

    ctx.beginPath();

    ctx.ellipse(
      cx,
      cy,
      rx,
      ry,
      0,
      0,
      Math.PI * 2
    );

    ctx.stroke();

    /*
     * Lesion
     */
    const distanceFromCenter =
      Math.abs(sliceIdx - lesionCenter);

    if (distanceFromCenter <= lesionSpan) {

      const lesionT =
        1 - distanceFromCenter / lesionSpan;

      const lesionRadius =
        9 + lesionT * 22;

      const lesionX =
        cx + rx * 0.34;

      const lesionY =
        cy - ry * 0.12;

      ctx.save();

      ctx.globalAlpha = opacity;

      ctx.fillStyle = '#E5384C';

      ctx.beginPath();

      const points = 9;

      for (let i = 0; i <= points; i++) {

        const angle =
          (i / points) * Math.PI * 2;

        const jitter =
          0.75 +
          rand(i * 4.4 + sliceIdx) * 0.5;

        const px =
          lesionX +
          Math.cos(angle) *
          lesionRadius *
          jitter;

        const py =
          lesionY +
          Math.sin(angle) *
          lesionRadius *
          0.8 *
          jitter;

        if (i === 0) {
          ctx.moveTo(px, py);
        } else {
          ctx.lineTo(px, py);
        }
      }

      ctx.closePath();
      ctx.fill();

      ctx.globalAlpha =
        Math.min(opacity + 0.25, 1);

      ctx.strokeStyle = '#FF6B7A';
      ctx.lineWidth = 1.4;

      ctx.stroke();

      ctx.restore();
    }
  }


}